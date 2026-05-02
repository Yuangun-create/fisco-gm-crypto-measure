pragma solidity ^0.4.25;

contract TimerPrecompiled {
    function getCurrentTimeMillis() public constant returns(uint256);
}

contract SM3XORTEST {
    TimerPrecompiled timer;
    bytes32 constant KEY = 0x3a4f9c8b7e6d5a2f1b4c8e9a7d6f5c3b2a1e9f8d7c6b5a4f3e2d1c0b9a8f7e6d;
    
    // 状态变量，减少局部变量
    bytes public testData;
    bytes public encData;
    bytes public decData;
    
    event TestResult(string result);
    
    constructor() public {
        timer = TimerPrecompiled(0x500c);
    }
    
    // dataSizeKB: 数据大小(KB)
    // rounds: 轮数
    function test(uint dataSizeKB, uint rounds) public returns(string) {
        require(dataSizeKB > 0 && dataSizeKB <= 100);
        require(rounds > 0 && rounds <= 10000);
        
        uint dataSize = dataSizeKB * 1024;
        
        // 初始化数据
        testData = new bytes(dataSize);
        for(uint i = 0; i < dataSize; i++) {
            testData[i] = byte(uint8(i % 256));
        }
        
        bytes32 hash1 = keccak256(testData);
        
        // 加密
        uint ms1 = doEncrypt(dataSize, rounds);
        
        // 解密
        uint ms2 = doDecrypt(dataSize, rounds);
        
        // 验证
        bytes32 hash2 = keccak256(decData);
        if(hash1 != hash2) {
            return "Error!";
        }
        
        // 计算速度
        uint mbps1 = calcSpeed(dataSize, rounds, ms1);
        uint mbps2 = calcSpeed(dataSize, rounds, ms2);
        
        // 返回结果
        return buildResult(dataSizeKB, rounds, ms1, mbps1, ms2, mbps2);
    }
    
    function doEncrypt(uint size, uint rounds) internal returns(uint) {
        encData = new bytes(size);
        
        uint256 t1 = timer.getCurrentTimeMillis();
        
        for(uint r = 0; r < rounds; r++) {
            bytes memory ks = genKeyStream(size);
            for(uint i = 0; i < size; i++) {
                encData[i] = testData[i] ^ ks[i];
            }
        }
        
        uint256 t2 = timer.getCurrentTimeMillis();
        return uint(t2 - t1);
    }
    
    function doDecrypt(uint size, uint rounds) internal returns(uint) {
        decData = new bytes(size);
        
        uint256 t1 = timer.getCurrentTimeMillis();
        
        for(uint r = 0; r < rounds; r++) {
            bytes memory ks = genKeyStream(size);
            for(uint i = 0; i < size; i++) {
                decData[i] = encData[i] ^ ks[i];
            }
        }
        
        uint256 t2 = timer.getCurrentTimeMillis();
        return uint(t2 - t1);
    }
    
    function genKeyStream(uint len) internal view returns(bytes) {
        bytes memory s = new bytes(len);
        bytes32 h = KEY;
        
        for(uint i = 0; i < len; i += 32) {
            h = keccak256(abi.encodePacked(h));
            uint rem = len - i;
            uint cp = rem < 32 ? rem : 32;
            for(uint j = 0; j < cp; j++) {
                s[i + j] = byte(h[j]);
            }
        }
        return s;
    }
    
    function calcSpeed(uint size, uint rounds, uint ms) internal pure returns(uint) {
        if(ms > 0) {
            return (size * rounds * 8) / (ms * 1000);
        }
        return 0;
    }
    
    function buildResult(uint kb, uint r, uint ms1, uint mbps1, uint ms2, uint mbps2) 
        internal pure returns(string) 
    {
        string memory res = uintToString(kb);
        res = concat(res, "KB x ");
        res = concat(res, uintToString(r));
        res = concat(res, " | Enc: ");
        res = concat(res, uintToString(ms1));
        res = concat(res, "ms/");
        res = concat(res, uintToString(mbps1));
        res = concat(res, "Mbps | Dec: ");
        res = concat(res, uintToString(ms2));
        res = concat(res, "ms/");
        res = concat(res, uintToString(mbps2));
        res = concat(res, "Mbps");
        return res;
    }
    
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
    
    function concat(string _a, string _b) internal pure returns (string) {
        bytes memory ba = bytes(_a);
        bytes memory bb = bytes(_b);
        bytes memory res = new bytes(ba.length + bb.length);
        uint k = 0;
        for (uint i = 0; i < ba.length; i++) {
            res[k++] = ba[i];
        }
        for (uint j = 0; j < bb.length; j++) {
            res[k++] = bb[j];
        }
        return string(res);
    }
}
