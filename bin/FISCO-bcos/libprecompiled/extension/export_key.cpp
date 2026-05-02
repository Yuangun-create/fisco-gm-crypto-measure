// export_key.cpp
#include <openssl/x509.h>
#include <openssl/ec.h>
#include <openssl/evp.h>
#include <openssl/bn.h>
#include <openssl/err.h>
#include <vector>
#include <fstream>
#include <iomanip>
#include <cstring>

static const uint8_t PUB_X[32] = {
    0xB4,0x93,0xEE,0x6C,0x23,0x72,0x30,0x6A,0x1B,0x0A,0x89,0xEE,0x36,0xE3,0x46,0x06,
    0x33,0xE4,0xF0,0xE7,0x81,0x0A,0x47,0x14,0x7B,0x9C,0x33,0xD9,0x7E,0x21,0x35,0xA4
};
static const uint8_t PUB_Y[32] = {
    0x9C,0x76,0xCE,0x60,0xD3,0x49,0xB2,0xE5,0x11,0xBA,0xC9,0x92,0x7E,0x97,0xD8,0x8F,
    0x0B,0x4B,0xB1,0x3B,0x04,0x04,0x6D,0x98,0x36,0xBE,0xB2,0x7B,0x38,0x1F,0xB9,0x0D
};

int main() {
    EC_KEY* ec = EC_KEY_new_by_curve_name(NID_sm2);
    const EC_GROUP* grp = EC_KEY_get0_group(ec);
    BIGNUM *x = BN_bin2bn(PUB_X, 32, nullptr);
    BIGNUM *y = BN_bin2bn(PUB_Y, 32, nullptr);
    EC_POINT* pt = EC_POINT_new(grp);
    EC_POINT_set_affine_coordinates_GFp(grp, pt, x, y, nullptr);
    EC_KEY_set_public_key(ec, pt);

    EVP_PKEY* pkey = EVP_PKEY_new();
    EVP_PKEY_set1_EC_KEY(pkey, ec);

    /* 导出 DER */
    int len = i2d_PUBKEY(pkey, nullptr);
    std::vector<uint8_t> der(len);
    uint8_t* tmp = der.data();
    i2d_PUBKEY(pkey, &tmp);

    /* 写成 C 数组 */
    std::ofstream ofs("key.inc");
    ofs << "static const uint8_t EVP_PKEY_DER[" << len << "] = {";
    for (int i = 0; i < len; ++i) {
        if (i % 16 == 0) ofs << "\n    ";
        ofs << "0x" << std::hex << std::setw(2) << std::setfill('0')
            << (int)der[i] << ",";
    }
    ofs << "\n};\n";
    ofs.close();

    EVP_PKEY_free(pkey);
    EC_KEY_free(ec);
    BN_free(x); BN_free(y);
    EC_POINT_free(pt);
    return 0;
}
