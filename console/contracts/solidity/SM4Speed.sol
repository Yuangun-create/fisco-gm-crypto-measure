pragma solidity ^0.4.25;

contract TimerPrecompiled {
    function getCurrentTimeMillis() public constant returns (uint256);
}

contract SM4Precompiled {
    function encrypt(bytes) public constant returns (bytes);
}

contract SM4Speed {
    TimerPrecompiled timer;
    SM4Precompiled sm4;

    uint public constant DATA_SIZE = 2 * 1024; // 2 KB
    uint public constant ROUNDS = 1;         // 可调

    event EncryptSpeed(uint Mbps, uint elapsedMs);

    constructor() public {
        timer = TimerPrecompiled(0x500c);
        sm4 = SM4Precompiled(0x500d);
    }

    /* 链上测速：只跑加密，不拿密文 */
    function test() public returns (uint Mbps, uint elapsedMs) {
        bytes memory testData = new bytes(DATA_SIZE);
        for (uint i = 0; i < DATA_SIZE; i++) {
            testData[i] = byte(uint8(i % 256));
        }

        uint256 start = timer.getCurrentTimeMillis();

        for (uint r = 0; r < ROUNDS; r++) {
            // 节点内：PKCS7 补齐 → CBC 加密 → 不返回密文
            bytes memory cipher = sm4.encrypt(testData);
            if (cipher.length == 0) return (0, 0);
        }

        uint256 end = timer.getCurrentTimeMillis();
        elapsedMs = end - start;
        if (elapsedMs > 0) {
            Mbps = (DATA_SIZE * ROUNDS * 8) / (elapsedMs * 1000);
        }
        emit EncryptSpeed(Mbps, elapsedMs);
    }
}
