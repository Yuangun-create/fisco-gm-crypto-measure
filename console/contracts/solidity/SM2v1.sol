pragma solidity ^0.4.25;

contract SM2Precompiled {
    function encrypt(string pubHex, bytes plaintext) public constant returns(uint256, uint256);
    function decrypt(string priHex, bytes ciphertext) public constant returns(uint256);
}

contract SM2 {
    SM2Precompiled sm2;

    uint public constant DATA_SIZE = 1 * 1024;
    uint public constant CHUNK_SIZE = 1024;
    uint256 public ent;
    uint256 public det;
    uint256 public enMbps;
    uint256 public deMbps;
    event Speed(uint256 enMbps, uint256 ent, uint256 deMbps, uint256 det);
    constructor() public {
        sm2 = SM2Precompiled(0x500b);
    }

    string public constant pubKey =  
"435B39CCA8F3B508C1488AFC67BE491A0F7BA07E581A0E4849A5CF70628A7E0A75DDBA78F15FEECB4C7895E2C1CDF5FE01DEBB2CDBADF45399CCF77BBA076A42"; 
    string public constant priKey =  
"1649AB77A00637BD5E2EFE283FBF353534AA7F7CB89463F208DDBC2920BB0DA0"; 
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
    
    function buildTestCipher() private returns (bytes memory) {
        // 1. 固定 C1（65 B）
        bytes memory C1 = "04245C26FB68B1DDDDDDB12C4B6BF9F2B6D5FE60A383B0D18D1C4144ABF17F6252E776CB9264C2A7E88E52B19903FDC47378F605E36811F5C07423A24B84400F01B8";

        // 2. 拼装任意长度 C2
        //bytes memory C2 = new bytes(klen);
        // 0.4.25 没有 memset，用循环
        //for (uint i = 0; i < klen; i++) {
        //    C2[i] = byte(0);
        //}
	bytes memory C2 = generateTestData();
        // 3. 固定 C3（32 B）
        bytes memory C3 = "9C3D7360C30156FAB7C80A0276712DA9D8094A634B766D3A285E07480653426D";

        // 4. 拼成 final cipher
        bytes memory cipher = new bytes(65 + DATA_SIZE + 32);
        uint offset = 0;
        // memcpy C1
        for (uint j = 0; j < 65; j++) {
            cipher[offset++] = C1[j];
        }
        // memcpy C2
        for (uint k = 0; k < DATA_SIZE; k++) {
            cipher[offset++] = C2[k];
        }
        // memcpy C3
        for (uint m = 0; m < 32; m++) {
            cipher[offset++] = C3[m];
        }
        return cipher;
    }
    
    function test() public returns (uint Mbps, uint256 elapsedMs) {
        bytes memory testData = generateTestData();
        (ent, det) = sm2.encrypt(pubKey, testData);
        if (ent > 0) {
            enMbps = (DATA_SIZE * 8) / (ent * 1000);
        }
        //elapsedMs2 = sm2.decrypt(priKey, buildTestCipher());
        if (det > 0) {
            deMbps = (DATA_SIZE * 8) / (det * 1000);
        }
        emit Speed(enMbps, ent, deMbps, det);
    }
}
