#pragma once
#include <libprecompiled/Precompiled.h>
#include <string>
#include <utility>

namespace dev { namespace precompiled {
class SM2Verify : public Precompiled {
public:
    SM2Verify();
    
    PrecompiledExecResult::Ptr call(
        std::shared_ptr<dev::blockverifier::ExecutiveContext>,
        bytesConstRef _param,
        Address const&,
        Address const&) override;
};
}}
