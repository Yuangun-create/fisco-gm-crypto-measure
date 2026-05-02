pragma solidity ^0.4.25;

contract SM2VerifyPrecompiled {
    function encrypt() public constant returns(string);
    function decrypt() public constant returns(string);
}

contract SM2Verify {
    SM2VerifyPrecompiled sm2;

    event EncryptResult(string en_result, string de_result);
    constructor() public {
        sm2 = SM2VerifyPrecompiled(0x500d);
    }
    //X||Y
    //string public constant pubKey =  
//"435B39CCA8F3B508C1488AFC67BE491A0F7BA07E581A0E4849A5CF70628A7E0A75DDBA78F15FEECB4C7895E2C1CDF5FE01DEBB2CDBADF45399CCF77BBA076A42"; 
    //bytes public constant testData = hex"656E6372797074696F6E207374616E64617264";
    //bytes public constant testData = "encryption standard";
    function test() public {
        string memory en_result = sm2.encrypt();
        string memory de_result = sm2.decrypt();
        emit EncryptResult(en_result, de_result);
    }
}
