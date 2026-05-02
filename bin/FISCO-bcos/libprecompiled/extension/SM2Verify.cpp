#include "SM2Verify.h"
#include <libethcore/ABI.h>
#include <libdevcrypto/Common.h>
#include <libdevcore/CommonData.h>
#include <openssl/evp.h>
#include <openssl/ec.h>
#include <openssl/bn.h>
#include <openssl/err.h>

using namespace dev;
using namespace dev::precompiled;
using namespace dev::eth;

/* ---------- 预编译方法 ---------- */
const char* const SM2_ENCRYPT_METHOD = "encrypt()";
const char* const SM2_DECRYPT_METHOD = "decrypt()";

SM2Verify::SM2Verify() {
    name2Selector[SM2_ENCRYPT_METHOD] = getFuncSelector(SM2_ENCRYPT_METHOD);
    name2Selector[SM2_DECRYPT_METHOD] = getFuncSelector(SM2_DECRYPT_METHOD);
}

static const char SM2_P[] =
    "8542D69E4C044F18E8B92435BF6FF7DE457283915C45517D722EDB8B08F1DFC3";
static const char SM2_A[] =
    "787968B4FA32C3FD2417842E73BBFEFF2F3C848B6831D7E0EC65228B3937E498";
static const char SM2_B[] =
    "63E4C6D3B23B0C849CF84241484BFE48F61D59A5B16BA06E6E12D1DA27C5249A";
static const char SM2_Gx[] =
    "421DEBD61B62EAB6746434EBC3CC315E32220B3BADD50BDC4C4E6C147FEDD43D";
static const char SM2_Gy[] =
    "0680512BCBB42C07D47349D2153B70C4E5D7FDFCBFA36EA1A85841B9E46E09A2";
static const char SM2_N[] =
    "8542D69E4C044F18E8B92435BF6FF7DD297720630485628D5AE74EE7C32E79B7";    

static BIGNUM* bn_from_hex(const char* hex)
{
    BIGNUM* r = BN_new();
    BN_hex2bn(&r, hex);
    return r;
}

static bytes bn2be32(BIGNUM* bn)
{
    bytes buf(32, 0);
    int n = BN_num_bytes(bn);
    if (n > 32) throw std::runtime_error("bn > 32 bytes");
    BN_bn2bin(bn, buf.data() + 32 - n);
    return buf;
}

static void append_bn_be(bytes& out, BIGNUM* bn, int len)
{
    bytes tmp(len, 0);
    int n = BN_num_bytes(bn);
    if (n > len) throw std::runtime_error("bn > len");
    BN_bn2bin(bn, tmp.data() + len - n);
    out.insert(out.end(), tmp.begin(), tmp.end());
}

static bytes sm3_hash(const bytes& in)
{
    bytes out(32);
    unsigned int len = 32;
    EVP_Digest(in.data(), in.size(), out.data(), &len, EVP_sm3(), nullptr);
    return out;
}

static bytes KDF(const bytes& Z, uint32_t klen)
{
    bytes t;
    uint32_t ct = 1;
    while (klen > 0) {
        bytes cb(4);
        cb[0] = (ct >> 24) & 0xFF;
        cb[1] = (ct >> 16) & 0xFF;
        cb[2] = (ct >> 8)  & 0xFF;
        cb[3] = ct & 0xFF;
        bytes h = sm3_hash(Z + cb);
        uint32_t take = (klen > 32) ? 32 : klen;
        t.insert(t.end(), h.begin(), h.begin() + take);
        klen -= take;
        ++ct;
    }
    return t; 
}

static EC_GROUP* sm2_group_new()
{
    BN_CTX* ctx = BN_CTX_new();
    BIGNUM *p = BN_new(), *a = BN_new(), *b = BN_new();
    BIGNUM *gx = BN_new(), *gy = BN_new(), *n = BN_new();
    EC_GROUP* grp = nullptr;

    BN_hex2bn(&p, SM2_P);
    BN_hex2bn(&a, SM2_A);
    BN_hex2bn(&b, SM2_B);
    BN_hex2bn(&gx, SM2_Gx);
    BN_hex2bn(&gy, SM2_Gy);
    BN_hex2bn(&n, SM2_N);

    grp = EC_GROUP_new_curve_GFp(p, a, b, ctx);
    EC_POINT* G = EC_POINT_new(grp);
    EC_POINT_set_affine_coordinates_GFp(grp, G, gx, gy, ctx);
    EC_GROUP_set_generator(grp, G, n, BN_value_one());

    EC_POINT_free(G);
    BN_free(p); BN_free(a); BN_free(b);
    BN_free(gx); BN_free(gy); BN_free(n);
    BN_CTX_free(ctx);
    return grp;
}

// SM2 加密
static bytes sm2_encrypt(const EC_GROUP* grp,const EC_POINT* PB,const bytes& plain,const BIGNUM* k)
{
    BN_CTX* ctx = BN_CTX_new();

    EC_POINT* C1 = EC_POINT_new(grp);
    EC_POINT_mul(grp, C1, k, nullptr, nullptr, ctx);
    BIGNUM *x1 = BN_new(), *y1 = BN_new();
    EC_POINT_get_affine_coordinates_GFp(grp, C1, x1, y1, ctx);
    bytes C1bin;
    C1bin.push_back(0x04);
    append_bn_be(C1bin, x1, 32);
    append_bn_be(C1bin, y1, 32);

    EC_POINT* kPB = EC_POINT_new(grp);
    EC_POINT_mul(grp, kPB, nullptr, PB, k, ctx);
    BIGNUM *x2 = BN_new(), *y2 = BN_new();
    EC_POINT_get_affine_coordinates_GFp(grp, kPB, x2, y2, ctx);
    bytes x2bin = bn2be32(x2), y2bin = bn2be32(y2);

    uint32_t klen = static_cast<uint32_t>(plain.size());
    bytes Z = x2bin;
    Z.insert(Z.end(), y2bin.begin(), y2bin.end());
    bytes t = KDF(Z, klen);

    bytes C2 = plain;
    for (uint32_t i = 0; i < klen; ++i) C2[i] ^= t[i];

    bytes tmp = x2bin;
    tmp.insert(tmp.end(), plain.begin(), plain.end());
    tmp.insert(tmp.end(), y2bin.begin(), y2bin.end());
    bytes C3 = sm3_hash(tmp);

    bytes cipher = C1bin;
    cipher.insert(cipher.end(), C2.begin(), C2.end());
    cipher.insert(cipher.end(), C3.begin(), C3.end());

    BN_free(x1); BN_free(y1); BN_free(x2); BN_free(y2);
    EC_POINT_free(C1); EC_POINT_free(kPB);
    BN_CTX_free(ctx);

    return cipher;
}

// SM2 解密
static bytes sm2_decrypt(EC_GROUP* grp,const EC_POINT* C1,const bytes& C2,const bytes& C3,const BIGNUM* dB,uint32_t klen)
{
    EC_POINT* S = EC_POINT_new(grp);
    EC_POINT_mul(grp, S, nullptr, C1, dB, nullptr);

    BIGNUM *x2 = BN_new(), *y2 = BN_new();
    EC_POINT_get_affine_coordinates_GFp(grp, S, x2, y2, nullptr);

    bytes x2bin = bn2be32(x2);
    bytes y2bin = bn2be32(y2);

    bytes Z = x2bin;
    Z.insert(Z.end(), y2bin.begin(), y2bin.end());
    bytes t = KDF(Z, klen);

    bytes plain = C2;
    for (uint32_t i = 0; i < klen; ++i) plain[i] ^= t[i];

    bytes tmp = x2bin;
    tmp.insert(tmp.end(), plain.begin(), plain.end());
    tmp.insert(tmp.end(), y2bin.begin(), y2bin.end());
    bytes u = sm3_hash(tmp);

    if (u != C3) throw std::runtime_error("Hash verification failed");

    BN_free(x2); BN_free(y2); EC_POINT_free(S);
    return plain;
}

/* ---------- 主调用函数 ---------- */
PrecompiledExecResult::Ptr SM2Verify::call(
    std::shared_ptr<dev::blockverifier::ExecutiveContext>,
    bytesConstRef _param,
    Address const&,
    Address const&)
{
    uint32_t func = getParamFunc(_param);
    auto result = m_precompiledExecResultFactory->createPrecompiledResult();
    ContractABI abi;

    try {
	if (func == name2Selector[SM2_ENCRYPT_METHOD]) {
    	    static const std::string pubkey = "435B39CCA8F3B508C1488AFC67BE491A0F7BA07E581A0E4849A5CF70628A7E0A75DDBA78F15FEECB4C7895E2C1CDF5FE01DEBB2CDBADF45399CCF77BBA076A42";
    	    static const std::string plainStr = "encryption standard";
    	    const std::string kHex = "4C62EEFD6ECFC2B95B92FD6C3D9575148AFA17425546D49018E5388D49DD7B4F";
    	    static const std::string expectedcipher = "04245c26fb68b1ddddb12c4b6bf9f2b6d5fe60a383b0d18d1c4144abf17f6252e776cb9264c2a7e88e52b19903fdc47378f605e36811f5c07423a24b84400f01b8650053a89b41c418b0c3aad00d886c002864679c3d7360c30156fab7c80a0276712da9d8094a634b766d3a285e07480653426d";
    
    	    bytes plainBytes(plainStr.begin(), plainStr.end());
    
    	    EC_GROUP* grp = sm2_group_new();
    	    BIGNUM *x = bn_from_hex(pubkey.substr(0, 64).c_str());
    	    BIGNUM *y = bn_from_hex(pubkey.substr(64, 64).c_str());
    	    EC_POINT* PB = EC_POINT_new(grp);
    	    if (!EC_POINT_set_affine_coordinates_GFp(grp, PB, x, y, nullptr) || !EC_POINT_is_on_curve(grp, PB, nullptr))
        	throw std::runtime_error("pub not on curve");

    	    BIGNUM* k = bn_from_hex(kHex.c_str());

    	    bytes cipher = sm2_encrypt(grp, PB, plainBytes, k);

    	    BN_free(x);
    	    BN_free(y);
    	    BN_free(k);
    	    EC_POINT_free(PB);
    	    EC_GROUP_free(grp);

    	    std::string cipherhex = toHex(cipher);
    	    std::string reply = (cipherhex == expectedcipher) ? "Encrypt Succeeded!" : "Encrypt Failed!";
    	    result->setExecResult(abi.abiIn("", reply));
	}
	else if (func == name2Selector[SM2_DECRYPT_METHOD]) {
    	    static const std::string priKey = "1649AB77A00637BD5E2EFE283FBF353534AA7F7CB89463F208DDBC2920BB0DA0";
    	    static const std::string cipherHex = "04245c26fb68b1ddddb12c4b6bf9f2b6d5fe60a383b0d18d1c4144abf17f6252e776cb9264c2a7e88e52b19903fdc47378f605e36811f5c07423a24b84400f01b8650053a89b41c418b0c3aad00d886c002864679c3d7360c30156fab7c80a0276712da9d8094a634b766d3a285e07480653426d";
    
    	    static const bytes expectedplain = {
        0x65, 0x6e, 0x63, 0x72, 0x79, 0x70, 0x74, 0x69, 
        0x6f, 0x6e, 0x20, 0x73, 0x74, 0x61, 0x6e, 0x64, 
        0x61, 0x72, 0x64
    };

    	    bytes cipherBytes = fromHex(cipherHex);
	    
    	    bytes x1_bytes(cipherBytes.begin() + 1, cipherBytes.begin() + 33);
    	    bytes y1_bytes(cipherBytes.begin() + 33, cipherBytes.begin() + 65);
    	    bytes C3(cipherBytes.end() - 32, cipherBytes.end());
    	    bytes C2(cipherBytes.begin() + 65, cipherBytes.end() - 32);
    	    uint32_t klen = static_cast<uint32_t>(C2.size());

    	    EC_GROUP* grp = sm2_group_new();
    	    if (!grp) throw std::runtime_error("sm2_group_new fail");
    	    BIGNUM *x1 = BN_new(), *y1 = BN_new();
    	    BN_bin2bn(x1_bytes.data(), 32, x1);
    	    BN_bin2bn(y1_bytes.data(), 32, y1);
    	    EC_POINT* C1 = EC_POINT_new(grp);
    	    if (!EC_POINT_set_affine_coordinates_GFp(grp, C1, x1, y1, nullptr) ||
    	        !EC_POINT_is_on_curve(grp, C1, nullptr))
    	        throw std::runtime_error("C1 not on curve");

    	    BIGNUM* dB = bn_from_hex(priKey.c_str());

    	    bytes plain = sm2_decrypt(grp, C1, C2, C3, dB, klen);
    	    BN_free(x1); BN_free(y1); BN_free(dB);
    	    EC_POINT_free(C1);EC_GROUP_free(grp);
    	    bool isSuccess = (plain == expectedplain);
    	    std::string reply = isSuccess ? "Decrypt Succeeded!" : "Decrypt Failed!";
    	    result->setExecResult(abi.abiIn("", reply));
	}
        else {
            BOOST_THROW_EXCEPTION(PrecompiledException("Unknown function"));
        }
    }
    catch (const std::exception& e) {
        PRECOMPILED_LOG(ERROR) << "SM2EncryptPrecompiled exception:" << e.what();
        BOOST_THROW_EXCEPTION(PrecompiledException(e.what()));
    }
    return result;
}
