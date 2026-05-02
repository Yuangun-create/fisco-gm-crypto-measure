package org.com.fisco;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.DynamicBytes;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32;
import org.fisco.bcos.sdk.abi.datatypes.generated.Uint256;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple1;
import org.fisco.bcos.sdk.abi.datatypes.generated.tuples.generated.Tuple2;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.contract.Contract;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.fisco.bcos.sdk.eventsub.EventCallback;
import org.fisco.bcos.sdk.model.CryptoType;
import org.fisco.bcos.sdk.model.TransactionReceipt;
import org.fisco.bcos.sdk.model.callback.TransactionCallback;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

@SuppressWarnings("unchecked")
public class SM3XorBench extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b506106d2806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806349dd107f1461005c5780637ca5b67914610073578063d17b485b146100ea575b600080fd5b34801561006857600080fd5b50610071610117565b005b34801561007f57600080fd5b506100e8600480360381019080803590602001908201803590602001908080601f016020809104026020016040519081016040528093929190818152602001838380828437820191505050505050919291929080356000191690602001909291905050506102fb565b005b3480156100f657600080fd5b506101156004803603810190808035906020019092919050505061046b565b005b60606000806000806000806000806000806104006040519080825280601f01601f19166020018201604052801561015d5781602001602082028038833980820191505090505b509a50600099505b6104008a10156101ea576101008a81151561017c57fe5b067f0100000000000000000000000000000000000000000000000000000000000000028b8b8151811015156101ad57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535089806001019a5050610165565b600180029850429750600096505b6040871015610278578a6040518082805190602001908083835b6020831015156102375780518252602082019150602081019050602083039250610212565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390209550888618945086806001019750506101f8565b4293508784039250620800009150620f4240838381151561029557fe5b0481151561029f57fe5b0490507f2706c3a31ad5099929a565f99a03e84ccc8e03f8acbe063e95fea7a02c248166610400846064840260405180848152602001838152602001828152602001935050505060405180910390a15050505050505050505050565b6000806000806000806000429650886040518082805190602001908083835b60208310151561033f578051825260208201915060208101905060208303925061031a565b6001836020036101000a03801982511681845116808217855250505050505090500191505060405180910390209550878618945042935086841115156103ed576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260078152602001807f747320657120300000000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b868403925060088951029150620f4240836064840281151561040b57fe5b0481151561041557fe5b0490507f2706c3a31ad5099929a565f99a03e84ccc8e03f8acbe063e95fea7a02c2481668951848360405180848152602001838152602001828152602001935050505060405180910390a1505050505050505050565b606060008060008060008060008060008a6040519080825280601f01601f1916602001820160405280156104ae5781602001602082028038833980820191505090505b509950600098505b8a89101561053957610100898115156104cb57fe5b067f0100000000000000000000000000000000000000000000000000000000000000028a8a8151811015156104fc57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535088806001019950506104b6565b600180029750429650896040518082805190602001908083835b6020831015156105785780518252602082019150602081019050602083039250610553565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040518091039020955087861894504293508684111515610626576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260078152602001807f747320657120300000000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b868403925060088a51029150620f4240836064840281151561064457fe5b0481151561064e57fe5b0490507f2706c3a31ad5099929a565f99a03e84ccc8e03f8acbe063e95fea7a02c2481668a51848360405180848152602001838152602001828152602001935050505060405180910390a150505050505050505050505600a165627a7a72305820f69b15a6618fa1d0209d812b43ee6a723e51f421b759ce85d98fcc35f02143e80029"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b506106d2806100206000396000f300608060405260043610610057576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff168063027d80091461005c57806351a0e62f146100d35780635bd27533146100ea575b600080fd5b34801561006857600080fd5b506100d1600480360381019080803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091929192908035600019169060200190929190505050610117565b005b3480156100df57600080fd5b506100e8610287565b005b3480156100f657600080fd5b506101156004803603810190808035906020019092919050505061046b565b005b6000806000806000806000429650886040518082805190602001908083835b60208310151561015b5780518252602082019150602081019050602083039250610136565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040518091039020955087861894504293508684111515610209576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260078152602001807f747320657120300000000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b868403925060088951029150620f4240836064840281151561022757fe5b0481151561023157fe5b0490507f8b167b538b77f920d1db629cdbfab41d97794452bd41e4a24f55257a62964e5e8951848360405180848152602001838152602001828152602001935050505060405180910390a1505050505050505050565b60606000806000806000806000806000806104006040519080825280601f01601f1916602001820160405280156102cd5781602001602082028038833980820191505090505b509a50600099505b6104008a101561035a576101008a8115156102ec57fe5b067f0100000000000000000000000000000000000000000000000000000000000000028b8b81518110151561031d57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535089806001019a50506102d5565b600180029850429750600096505b60408710156103e8578a6040518082805190602001908083835b6020831015156103a75780518252602082019150602081019050602083039250610382565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040518091039020955088861894508680600101975050610368565b4293508784039250620800009150620f4240838381151561040557fe5b0481151561040f57fe5b0490507f8b167b538b77f920d1db629cdbfab41d97794452bd41e4a24f55257a62964e5e610400846064840260405180848152602001838152602001828152602001935050505060405180910390a15050505050505050505050565b606060008060008060008060008060008a6040519080825280601f01601f1916602001820160405280156104ae5781602001602082028038833980820191505090505b509950600098505b8a89101561053957610100898115156104cb57fe5b067f0100000000000000000000000000000000000000000000000000000000000000028a8a8151811015156104fc57fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535088806001019950506104b6565b600180029750429650896040518082805190602001908083835b6020831015156105785780518252602082019150602081019050602083039250610553565b6001836020036101000a0380198251168184511680821785525050505050509050019150506040518091039020955087861894504293508684111515610626576040517fc703cb120000000000000000000000000000000000000000000000000000000081526004018080602001828103825260078152602001807f747320657120300000000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b868403925060088a51029150620f4240836064840281151561064457fe5b0481151561064e57fe5b0490507f8b167b538b77f920d1db629cdbfab41d97794452bd41e4a24f55257a62964e5e8a51848360405180848152602001838152602001828152602001935050505060405180910390a150505050505050505050505600a165627a7a72305820628dbfaf30682b3337ceddd7631c5bc0cbe8003e9ef4658af01911a0aef985c00029"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":false,\"inputs\":[],\"name\":\"bench\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"input\",\"type\":\"bytes\"},{\"name\":\"key\",\"type\":\"bytes32\"}],\"name\":\"benchWithParams\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"dataSize\",\"type\":\"uint256\"}],\"name\":\"benchSize\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"inputBytes\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"timeSec\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"MbpsTimes100\",\"type\":\"uint256\"}],\"name\":\"Bench\",\"type\":\"event\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_BENCH = "bench";

    public static final String FUNC_BENCHWITHPARAMS = "benchWithParams";

    public static final String FUNC_BENCHSIZE = "benchSize";

    public static final Event BENCH_EVENT = new Event("Bench", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    protected SM3XorBench(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public TransactionReceipt bench() {
        final Function function = new Function(
                FUNC_BENCH, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] bench(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_BENCH, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForBench() {
        final Function function = new Function(
                FUNC_BENCH, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public TransactionReceipt benchWithParams(byte[] input, byte[] key) {
        final Function function = new Function(
                FUNC_BENCHWITHPARAMS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(input), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32(key)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] benchWithParams(byte[] input, byte[] key, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_BENCHWITHPARAMS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(input), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32(key)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForBenchWithParams(byte[] input, byte[] key) {
        final Function function = new Function(
                FUNC_BENCHWITHPARAMS, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.DynamicBytes(input), 
                new org.fisco.bcos.sdk.abi.datatypes.generated.Bytes32(key)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<byte[], byte[]> getBenchWithParamsInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_BENCHWITHPARAMS, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}, new TypeReference<Bytes32>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<byte[], byte[]>(

                (byte[]) results.get(0).getValue(), 
                (byte[]) results.get(1).getValue()
                );
    }

    public TransactionReceipt benchSize(BigInteger dataSize) {
        final Function function = new Function(
                FUNC_BENCHSIZE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(dataSize)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] benchSize(BigInteger dataSize, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_BENCHSIZE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(dataSize)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForBenchSize(BigInteger dataSize) {
        final Function function = new Function(
                FUNC_BENCHSIZE, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(dataSize)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getBenchSizeInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_BENCHSIZE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
                );
    }

    public List<BenchEventResponse> getBenchEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(BENCH_EVENT, transactionReceipt);
        ArrayList<BenchEventResponse> responses = new ArrayList<BenchEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BenchEventResponse typedResponse = new BenchEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.inputBytes = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.timeSec = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.MbpsTimes100 = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeBenchEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(BENCH_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeBenchEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(BENCH_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public static SM3XorBench load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new SM3XorBench(contractAddress, client, credential);
    }

    public static SM3XorBench deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(SM3XorBench.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }

    public static class BenchEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger inputBytes;

        public BigInteger timeSec;

        public BigInteger MbpsTimes100;
    }
}
