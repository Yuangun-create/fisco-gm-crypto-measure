/*
 * @CopyRight:
 * FISCO-BCOS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FISCO-BCOS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FISCO-BCOS.  If not, see <http://www.gnu.org/licenses/>
 * (c) 2016-2018 fisco-dev contributors.
 */
/** @file TimerPrecompiled.cpp
 *  @author Jiayi Liao
 *  @date 20251007
 */
#include "TimerPrecompiled.h"
#include <libblockverifier/ExecutiveContext.h>
#include <libethcore/ABI.h>
#include <chrono>

using namespace dev;
using namespace dev::blockverifier;
using namespace dev::precompiled;

/*
contract Timer {
    function getCurrentTimeMillis() public constant returns(uint256);
}
*/

// 获取当前时间戳（毫秒）
const char* const TIMER_METHOD_GET_TIME_MILLIS = "getCurrentTimeMillis()";

TimerPrecompiled::TimerPrecompiled()
{
    name2Selector[TIMER_METHOD_GET_TIME_MILLIS] = getFuncSelector(TIMER_METHOD_GET_TIME_MILLIS);
}

std::string TimerPrecompiled::toString()
{
    return "Timer";
}

PrecompiledExecResult::Ptr TimerPrecompiled::call(
    dev::blockverifier::ExecutiveContext::Ptr _context, bytesConstRef _param,
    Address const& _origin, Address const&)
{
    // 标记未使用的参数以避免编译警告
    (void)_context;
    (void)_origin;
    
    PRECOMPILED_LOG(TRACE) << LOG_BADGE("TimerPrecompiled") << LOG_DESC("call")
                           << LOG_KV("param", toHex(_param));

    // 解析函数名
    uint32_t func = getParamFunc(_param);
    auto callResult = m_precompiledExecResultFactory->createPrecompiledResult();
    callResult->gasPricer()->setMemUsed(_param.size());
    dev::eth::ContractABI abi;

    if (func == name2Selector[TIMER_METHOD_GET_TIME_MILLIS])
    {
        // 获取当前时间戳（毫秒）
        auto now = std::chrono::system_clock::now();
        auto timeMillis = std::chrono::duration_cast<std::chrono::milliseconds>(
            now.time_since_epoch()).count();
        
        PRECOMPILED_LOG(INFO) << LOG_BADGE("TimerPrecompiled") 
                              << LOG_DESC("getCurrentTimeMillis")
                              << LOG_KV("timeMillis", timeMillis);
        
        callResult->setExecResult(abi.abiIn("", u256(timeMillis)));
    }
    else
    {
        // 未知函数调用
        PRECOMPILED_LOG(ERROR) << LOG_BADGE("TimerPrecompiled") 
                               << LOG_DESC("unknown function")
                               << LOG_KV("func", func);
        callResult->setExecResult(abi.abiIn("", u256(int32_t(CODE_UNKNOW_FUNCTION_CALL))));
    }

    return callResult;
}
