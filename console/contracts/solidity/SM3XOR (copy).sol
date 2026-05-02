pragma solidity ^0.4.25;

contract SM3XOR {
    bytes32 constant KEY = 0x3a4f9c8b7e6d5a2f1b4c8e9a7d6f5c3b2a1e9f8d7c6b5a4f3e2d1c0b9a8f7e6d;
    
    event Result(string mode, uint blockSizeKB, uint totalMB, uint elapsedSec, uint mbps);
    
    // isEncrypt: true=加密, false=解密
    // blockSizeKB: 块大小(KB)
    // totalMB: 总数据量(MB)
    function bench(bool isEncrypt, uint blockSizeKB, uint totalMB) public returns(uint sec, uint mbps) {
        
        // 创建数据块
        uint blockSize = blockSizeKB * 1024;
        bytes memory data = new bytes(blockSize);
        for(uint i = 0; i < blockSize; i++) {
            data[i] = byte(uint8(i % 256));
        }
        
        uint t0 = now;
        uint rounds = (totalMB * 1024) / blockSizeKB;
        
        if(isEncrypt) {
            // 加密：hash(data) XOR key
            for(uint r = 0; r < rounds; r++) {
                bytes32 encrypted = keccak256(data) ^ KEY;
                if(encrypted == 0x0) data[0] = byte(uint8(r));
            }
        } else {
            // 解密：(hash(data) XOR key) XOR key
            for(uint j = 0; j < rounds; j++) {
                bytes32 decrypted = (keccak256(data) ^ KEY) ^ KEY;
                if(decrypted == 0x0) data[0] = byte(uint8(j));
            }
        }
        
        sec = now - t0;
        require(sec > 0, "Too fast, increase totalMB");
        
        mbps = (totalMB * 8192) / sec;
        
        emit Result(isEncrypt ? "Encrypt" : "Decrypt", blockSizeKB, totalMB, sec, mbps);
        return (sec, mbps);
    }
}
