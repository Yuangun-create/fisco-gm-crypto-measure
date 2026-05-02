pragma solidity ^0.4.25;
contract HelloWorldPrecompiled{
    function get() public constant returns(string);
    function set(string n);
}
