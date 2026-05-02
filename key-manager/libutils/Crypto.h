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
/**
 * @brief : key-manager server
 * @author: jimmyshi
 * @date: 2018-12-04
 */

#pragma once

#include "Common.h"
#include "CommonData.h"
#include "vector_ref.h"
#include <memory>
#include <string>

namespace dev
{
// SHA-3 convenience routines.

class Crypto
{
public:
    using Ptr = std::shared_ptr<Crypto>;

public:
    Crypto() = default;
    virtual ~Crypto() = default;

public:
    virtual bytes aesCBCEncrypt(bytesConstRef _plainData, bytesConstRef _key) = 0;
    virtual bytes aesCBCDecrypt(bytesConstRef _cypherData, bytesConstRef _key) = 0;
    virtual bytes uniformKey(bytesConstRef _readableKeyBytes) = 0;
};

}  // namespace dev