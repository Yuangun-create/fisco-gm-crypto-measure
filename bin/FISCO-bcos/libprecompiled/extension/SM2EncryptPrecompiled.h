#pragma once
#include <libprecompiled/Precompiled.h>
#include <string>
#include <utility>

namespace dev { namespace precompiled {
class SM2Speed : public Precompiled {
public:
    SM2Speed();
    
    PrecompiledExecResult::Ptr call(
        std::shared_ptr<dev::blockverifier::ExecutiveContext>,
        bytesConstRef _param,
        Address const&,
        Address const&) override;
};
}}
