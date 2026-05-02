/*pragma solidity ^0.4.25;

contract TimerPrecompiled {
    function getCurrentTimeMillis() public constant returns(uint256);
}

contract SM3XOR {
    TimerPrecompiled timer;
    bytes32 constant KEY = 0x3a4f9c8b7e6d5a2f1b4c8e9a7d6f5c3b2a1e9f8d7c6b5a4f3e2d1c0b9a8f7e6d;
    
    uint constant DATA_SIZE = 3 * 1024; // 30KB
    uint constant ROUNDS = 10; // 10轮
    
    // 存储原始数据和加密数据
    bytes public originalData;
    bytes public encryptedData;
    
    event TestResult(string result);
    
    constructor() public {
        timer = TimerPrecompiled(0x500c);
    }
    
    // 测试函数
    function test() public returns(string result) {
        // 创建原始数据
        originalData = new bytes(DATA_SIZE);
        bytes memory chunkData = new bytes(CHUNK_SIZE);
        for (uint i = 0; i < 1024; i++) {
            chunkData[i] = byte(uint8(i % 256));
        }

        uint repeatCount = DATA_SIZE / 1024;
        for (uint j = 0; j < repeatCount; j++) {
            for (uint k = 0; k < CHUNK_SIZE; k++) {
                originalData[j * CHUNK_SIZE + k] = chunkData[j];
            }
        }
        // 保存原始数据的哈希用于验证
        bytes32 originalHash = keccak256(originalData);
        
        // 加密测试
        uint encMbps = testEncrypt();
        
        // 解密测试
        bytes memory decryptedData = new bytes(DATA_SIZE);
        uint decMbps = testDecrypt(decryptedData);
        
        // 验证解密后的数据是否与原始数据相同
        bytes32 decryptedHash = keccak256(decryptedData);
        
        if(originalHash != decryptedHash) {
            return "Error: Decryption failed!";
        }
        
        // 验证通过，构造结果
        result = strConcat("Encrypt: ", uintToString(encMbps));
        result = strConcat(result, " Mbps, Decrypt: ");
        result = strConcat(result, uintToString(decMbps));
        result = strConcat(result, " Mbps [OK]");
        
        emit TestResult(result);
        return result;
    }
    
    // 加密测试
    function testEncrypt() internal returns(uint) {
        encryptedData = new bytes(DATA_SIZE);
        
        uint256 start = timer.getCurrentTimeMillis();
        
        for(uint r = 0; r < ROUNDS; r++) {
            // 生成与明文等长的密钥流（通过迭代哈希）
            bytes memory keyStream = generateKeyStream(DATA_SIZE);
            
            // 明文 XOR 密钥流 = 密文
            for(uint i = 0; i < DATA_SIZE; i++) {
                encryptedData[i] = originalData[i] ^ keyStream[i];
            }
        }
        
        uint256 end = timer.getCurrentTimeMillis();
        uint elapsed = uint(end - start);
        
        if(elapsed > 0) {
            return (DATA_SIZE * ROUNDS * 8) / (elapsed * 1000);
        }
        return 0;
    }
    
    // 解密测试
    function testDecrypt(bytes decrypted) internal returns(uint) {
        uint256 start = timer.getCurrentTimeMillis();
        
        for(uint r = 0; r < ROUNDS; r++) {
            // 生成相同的密钥流（通过迭代哈希）
            bytes memory keyStream = generateKeyStream(DATA_SIZE);
            
            // 密文 XOR 密钥流 = 明文
            for(uint i = 0; i < DATA_SIZE; i++) {
                decrypted[i] = encryptedData[i] ^ keyStream[i];
            }
        }
        
        uint256 end = timer.getCurrentTimeMillis();
        uint elapsed = uint(end - start);
        
        if(elapsed > 0) {
            return (DATA_SIZE * ROUNDS * 8) / (elapsed * 1000);
        }
        return 0;
    }
    
    // 生成密钥流：通过SM3(keccak256)迭代哈希
    function generateKeyStream(uint length) internal view returns(bytes) {
        bytes memory stream = new bytes(length);
        bytes32 hash = KEY;
        
        // 迭代哈希生成密钥流
        for(uint i = 0; i < length; i += 32) {
            // SM3迭代：hash = SM3(hash)
            hash = keccak256(abi.encodePacked(hash));
            
            // 填充密钥流
            uint remaining = length - i;
            uint copyLen = remaining < 32 ? remaining : 32;
            
            for(uint j = 0; j < copyLen; j++) {
                stream[i + j] = byte(hash[j]);
            }
        }
        
        return stream;
    }
    
    // uint转string
    function uintToString(uint v) internal pure returns (string) {
        if (v == 0) return "0";
        
        uint len;
        uint temp = v;
        while (temp != 0) {
            len++;
            temp /= 10;
        }
        
        bytes memory s = new bytes(len);
        uint k = len - 1;
        while (v != 0) {
            s[k--] = byte(uint8(48 + v % 10));
            v /= 10;
        }
        
        return string(s);
    }
    
    // 字符串拼接
    function strConcat(string _a, string _b) internal pure returns (string) {
        bytes memory ba = bytes(_a);
        bytes memory bb = bytes(_b);
        bytes memory result = new bytes(ba.length + bb.length);
        
        uint k = 0;
        for (uint i = 0; i < ba.length; i++) {
            result[k++] = ba[i];
        }
        for (uint j = 0; j < bb.length; j++) {
            result[k++] = bb[j];
        }
        
        return string(result);
    }
}*/
pragma solidity ^0.4.25;

/*----------  预编译时间戳 ----------*/
contract TimerPrecompiled {
    function getCurrentTimeMillis() public constant returns (uint256);
}

/*----------  主合约 ----------*/
contract SM3XOR {
    TimerPrecompiled constant timer = TimerPrecompiled(0x500c);

    bytes32 constant KEY = 0x3a4f9c8b7e6d5a2f1b4c8e9a7d6f5c3b2a1e9f8d7c6b5a4f3e2d1c0b9a8f7e6d;

    uint public constant DATA_SIZE = 20 * 1024;   // 30 KB
    uint public constant ROUNDS = 25;  // 每 32 字节哈希轮数

    bytes public originalData;     // 原始明文
    bytes public encryptedData;    // 密文

    event TestResult(string result);

    /*--------------------------------------------------------*
     *  入口：一次性完成「生成明文 → 加密 → 解密 → 测速 → 验签」  *
     *--------------------------------------------------------*/
    function test() public returns (string) {
        /*---- 1. 生成原始数据 ----*/
        originalData = new bytes(DATA_SIZE);
        bytes memory chunkData = new bytes(1024);
        
        for (uint i = 0; i < 1024; i++) {
            chunkData[i] = byte(uint8(i % 256));
        }

        uint repeatCount = DATA_SIZE / 1024;
        for (uint j = 0; j < repeatCount; j++) {
            for (uint k = 0; k < 1024; k++) {
                originalData[j * 1024 + k] = chunkData[j];
            }
        }
        bytes32 hash0 = keccak256(originalData);

        /*---- 2. 加密测速 ----*/
        uint encMbps = testEncrypt();

        /*---- 3. 解密测速 ----*/
        bytes memory decrypted = new bytes(DATA_SIZE);
        uint decMbps = testDecrypt(decrypted);

        /*---- 4. 正确性校验 ----*/
        if (keccak256(decrypted) != hash0) {
            return "Error: Decryption failed!";
        }

        /*---- 5. 拼装结果 ----*/
        string memory out = "Encrypt: ";
        out = strConcat(out, uintToString(encMbps));
        out = strConcat(out, " Mbps, Decrypt: ");
        out = strConcat(out, uintToString(decMbps));
        out = strConcat(out, " Mbps [OK]");

        emit TestResult(out);
        return out;
    }

    /*--------------------------------------------------------*
     *  加密：仅一次密钥流生成 → 整体异或 → 测速               *
     *--------------------------------------------------------*/
    function testEncrypt() internal returns (uint Mbps) {
        encryptedData = new bytes(DATA_SIZE);

        uint256 t0 = timer.getCurrentTimeMillis();
        bytes memory ks = generateKeyStream(DATA_SIZE);
        for (uint i = 0; i < DATA_SIZE; i++) {
            encryptedData[i] = originalData[i] ^ ks[i];
        }
        uint256 t1 = timer.getCurrentTimeMillis();

        uint elapsed = t1 - t0;
        if (elapsed == 0) return 0;
        return (DATA_SIZE * 8) / (elapsed * 1000);   // Mbps
    }

    /*--------------------------------------------------------*
     *  解密：相同密钥流 → 整体异或 → 测速                     *
     *--------------------------------------------------------*/
    function testDecrypt(bytes decrypted) internal returns (uint Mbps) {
        uint256 t0 = timer.getCurrentTimeMillis();
        bytes memory ks = generateKeyStream(DATA_SIZE);
        for (uint i = 0; i < DATA_SIZE; i++) {
            decrypted[i] = encryptedData[i] ^ ks[i];
        }
        uint256 t1 = timer.getCurrentTimeMillis();

        uint elapsed = t1 - t0;
        if (elapsed == 0) return 0;
        return (DATA_SIZE * 8) / (elapsed * 1000);
    }

    /*--------------------------------------------------------*
     *  密钥流生成：每 32 字节都迭代 ROUNDS 次 keccak256        *
     *--------------------------------------------------------*/
    function generateKeyStream(uint len) internal view returns (bytes) {
        bytes memory stream = new bytes(len);
        bytes32 h = KEY;
        for (uint i = 0; i < len; i += 32) {
            // 多轮哈希
            for (uint r = 0; r < ROUNDS; r++) {
                h = keccak256(abi.encodePacked(h));
            }
            uint remaining = len - i;
            uint copy = remaining < 32 ? remaining : 32;
            for (uint j = 0; j < copy; j++) {
                stream[i + j] = byte(h[j]);
            }
        }
        return stream;
    }

    /*--------------------  工具函数  --------------------*/
    function uintToString(uint v) internal pure returns (string) {
        if (v == 0) return "0";
        uint len;
        for (uint t = v; t > 0; t /= 10) len++;
        bytes memory s = new bytes(len);
        for (uint k = len - 1; k >= 0; k--) {
            s[k] = byte(uint8(48 + v % 10));
            v /= 10;
        }
        return string(s);
    }

    function strConcat(string a, string b) internal pure returns (string) {
        bytes memory ba = bytes(a);
        bytes memory bb = bytes(b);
        bytes memory r = new bytes(ba.length + bb.length);
        uint k = 0;
        for (uint i = 0; i < ba.length; i++) r[k++] = ba[i];
        for (uint j = 0; j < bb.length; j++) r[k++] = bb[j];
        return string(r);
    }
}
