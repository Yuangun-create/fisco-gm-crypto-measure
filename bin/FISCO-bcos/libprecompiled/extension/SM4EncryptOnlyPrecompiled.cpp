#include "SM4EncryptOnlyPrecompiled.h"
#include <libethcore/ABI.h>
#include "libdevcrypto/SM4Crypto.h"   // 已经存在

using namespace dev;
using namespace dev::precompiled;
using namespace dev::eth;

static const char* const SM4_ENCRYPT_METHOD = "encrypt(bytes)";
/* ---------- 硬编码 16 字节全 0 Key & IV ---------- */
static const uint8_t SM4_KEY[16] = {0};
static const uint8_t SM4_IV[16]  = {0};

/* ---------- 纯加密 dry-run ---------- */
static void sm4_encrypt_dryrun(const bytes& plain)
{
    // PKCS7 自动在 SM4Crypto 里完成
    dev::crypto::sm4Encrypt(plain.data(), plain.size(),
                            SM4_KEY, 16, SM4_IV);
    /* 不返回、不分配、不计密文 */
}

/* ---------- 预编译入口 ---------- */
SM4EncryptOnlyPrecompiled::SM4EncryptOnlyPrecompiled()
{
    name2Selector[SM4_ENCRYPT_METHOD] = getFuncSelector(SM4_ENCRYPT_METHOD);
}

PrecompiledExecResult::Ptr SM4EncryptOnlyPrecompiled::call(
    std::shared_ptr<dev::blockverifier::ExecutiveContext>,
    bytesConstRef _param,
    const Address&,
    const Address&)
{
    auto result = m_precompiledExecResultFactory->createPrecompiledResult();
    dev::eth::ContractABI abi;

    bytes plain;
    abi.abiOut(_param, plain);
    if (plain.empty()) throw std::runtime_error("empty plaintext");

    sm4_encrypt_dryrun(plain);

    result->setExecResult(bytes());
    return result;
}
