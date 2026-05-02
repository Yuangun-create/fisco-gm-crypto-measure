Description

This repository contains a prototype for testing and verifying the performance of Chinese National Cryptographic Algorithms (SM2 encryption/decryption, SM3 hash) in the FISCO BCOS GM (National Secret) blockchain scenario.
By enabling GM compilation in the underlying FISCO-BCOS kernel and using precompiled contracts to improve computational efficiency, this tool provides methods and sample code for measuring the execution time of a single algorithm operation.

Prerequisites
- cmake
- GCC 7.0 or higher
- FISCO BCOS source code

Build & Deployment
1. Compile FISCO-BCOS with GM enabled
cd ~/fisco/bin/FISCO-BCOS
mkdir build && cd build
cmake .. -DBUILD_GM=ON
make -j$(nproc)
2. Node Deployment
After compilation, the executable binary is located at FISCO-BCOS/build/bin/fisco-bcos.
Follow these steps to update the node:
   Stop the currently running test node.
   Replace the binary file under nodes/127.0.0.1/ with the newly generated fisco-bcos.
   Restart the node and console to start performance testing via precompiled contracts.
