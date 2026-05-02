pragma solidity ^0.4.25;

contract TimerPrecompiled {
    function getCurrentTimeMillis() public constant returns(uint256);
}

contract SM2Precompiled {
    function generateKeyPair() public constant returns(string, string);
    //function encrypt(string pubHex, bytes plaintext) public constant returns(bytes);
    function decrypt(string priHex, bytes ciphertext) public constant returns(bytes);
    //function encrypt(bytes plaintext) public constant returns(bytes text);
    function encrypt(string pubHex, bytes plaintext) public constant returns(uint256);
}

contract SM2 {
    TimerPrecompiled  timer;
    SM2Precompiled sm2;

    uint public constant DATA_SIZE = 16 * 1024; // 20 KB
    uint public constant CHUNK_SIZE = 1024;
    //event EncryptSpeed(bytes ret, uint len, uint Mbps, uint elapsedMs);
    //event EncryptSpeed(uint len, uint Mbps, uint elapsedMs);
    event EncryptSpeed(uint Mbps, uint256 elapsedMs);
    constructor() public {
        timer = TimerPrecompiled(0x500c);
        sm2 = SM2Precompiled(0x500b);
    }

    //string public constant pubKey =  "04B493EE6C2372306A1B0A89EE36E3460633E4F0E7810A47147B9C33D97E2135A49C76CE60D349B2E511BAC9927E97D88F0B4BB13B04046D9836BEB27B381FB90D";
        string public constant pubKey =  
"435B39CCA8F3B508C1488AFC67BE491A0F7BA07E581A0E4849A5CF70628A7E0A75DDBA78F15FEECB4C7895E2C1CDF5FE01DEBB2CDBADF45399CCF77BBA076A42"; 

    function generateTestData() private returns (bytes memory) {
        bytes memory testData = new bytes(DATA_SIZE);
        bytes memory chunkData = new bytes(CHUNK_SIZE);
        
        for (uint i = 0; i < CHUNK_SIZE; i++) {
            chunkData[i] = byte(uint8(i % 256));
        }

        uint repeatCount = DATA_SIZE / CHUNK_SIZE;
        for (uint j = 0; j < repeatCount; j++) {
            for (uint k = 0; k < CHUNK_SIZE; k++) {
                testData[j * CHUNK_SIZE + k] = chunkData[j];
            }
        }
        return testData;
    }
    
    function test() public returns (uint Mbps, uint256 elapsedMs) {
        bytes memory testData = generateTestData();
        elapsedMs = sm2.encrypt(pubKey, testData);
        if (elapsedMs > 0) {
            Mbps = (DATA_SIZE * 8) / (elapsedMs * 1000);
        }
        emit EncryptSpeed(Mbps, elapsedMs);
    }
}
