pragma solidity ^0.4.25;

contract SM2Precompiled {
    function encrypt(string pubHex, bytes plaintext) public constant returns(uint256);
    function decrypt(string priHex) public constant returns(uint256);
}

contract SM2 {
    SM2Precompiled sm2;

    uint public constant DATA_SIZE = 25 * 1024;
    uint public constant CHUNK_SIZE = 1024;
    event EncryptSpeed(uint Mbps, uint256 elapsedMs);
    event DecryptSpeed(uint Mbps, uint256 elapsedMs);
    constructor() public {
        sm2 = SM2Precompiled(0x500b);
    }

    string public constant pubKey =  
"435B39CCA8F3B508C1488AFC67BE491A0F7BA07E581A0E4849A5CF70628A7E0A75DDBA78F15FEECB4C7895E2C1CDF5FE01DEBB2CDBADF45399CCF77BBA076A42"; 
    string public constant priKey = "1649AB77A00637BD5E2EFE283FBF353534AA7F7CB89463F208DDBC2920BB0DA0";
    
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

    function entest() public returns (uint Mbps, uint256 elapsedMs) {
        bytes memory testData = generateTestData();
        elapsedMs = sm2.encrypt(pubKey, testData);
        if (elapsedMs > 0) {
            Mbps = (DATA_SIZE * 8 * 3) / (elapsedMs * 1000);
        }
        emit EncryptSpeed(Mbps, elapsedMs);
    }
    
    function detest() public returns (uint Mbps, uint256 elapsedMs) {
        elapsedMs = sm2.decrypt(priKey);
        if (elapsedMs > 0) {
            Mbps = (DATA_SIZE * 8 * 3) / (elapsedMs * 1000);
        }
        emit DecryptSpeed(Mbps, elapsedMs);
    }
}
