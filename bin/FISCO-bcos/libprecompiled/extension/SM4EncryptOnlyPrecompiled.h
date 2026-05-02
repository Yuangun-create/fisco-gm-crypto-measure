#pragma once
#include <libprecompiled/Common.h>

namespace dev{ namespace precompiled {
class SM4EncryptOnlyPrecompiled : public Precompiled {
public:
    SM4EncryptOnlyPrecompiled();
    PrecompiledExecResult::Ptr call(
        std::shared_ptr<dev::blockverifier::ExecutiveContext>,
        bytesConstRef _param,
        const Address& _origin,
        const Address& _sender) override;
};
}}  // namespace dev::precompiled
