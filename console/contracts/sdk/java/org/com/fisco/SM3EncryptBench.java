package org.com.fisco;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.fisco.bcos.sdk.abi.FunctionReturnDecoder;
import org.fisco.bcos.sdk.abi.TypeReference;
import org.fisco.bcos.sdk.abi.datatypes.Event;
import org.fisco.bcos.sdk.abi.datatypes.Function;
import org.fisco.bcos.sdk.abi.datatypes.Type;
import org.fisco.bcos.sdk.abi.datatypes.Utf8String;
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
public class SM3EncryptBench extends Contract {
    public static final String[] BINARY_ARRAY = {"608060405234801561001057600080fd5b50610583806100206000396000f300608060405260043610610083576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806318d934101461008857806336b7eab7146100ba578063370158ea146100ec578063378f0e8a1461011e5780639f4c4d0614610150578063a3e7ca5014610182578063b82196a1146101ca575b600080fd5b34801561009457600080fd5b5061009d6101fc565b604051808381526020018281526020019250505060405180910390f35b3480156100c657600080fd5b506100cf610212565b604051808381526020018281526020019250505060405180910390f35b3480156100f857600080fd5b50610101610228565b604051808381526020018281526020019250505060405180910390f35b34801561012a57600080fd5b50610133610235565b604051808381526020018281526020019250505060405180910390f35b34801561015c57600080fd5b5061016561024a565b604051808381526020018281526020019250505060405180910390f35b34801561018e57600080fd5b506101ad6004803603810190808035906020019092919050505061025f565b604051808381526020018281526020019250505060405180910390f35b3480156101d657600080fd5b506101df610542565b604051808381526020018281526020019250505060405180910390f35b60008061020a6101f461025f565b915091509091565b60008061022061040061025f565b915091509091565b6000804342915091509091565b600080610242600a61025f565b915091509091565b600080610257606461025f565b915091509091565b600080606060008060008060006104006040519080825280601f01601f1916602001820160405280156102a15781602001602082028038833980820191505090505b509550600094505b61040085101561032e57610100858115156102c057fe5b067f01000000000000000000000000000000000000000000000000000000000000000286868151811015156102f157fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535084806001019550506102a9565b429350600092505b610400890283101561044f57856040518082805190602001908083835b6020831015156103785780518252602082019150602081019050602083039250610353565b6001836020036101000a038019825116818451168082178552505050505050905001915050604051809103902091507f3a4f9c8b7e6d5a2f1b4c8e9a7d6f5c3b2a1e9f8d7c6b5a4f3e2d1c0b9a8f7e6d6001028218905060006001028160001916141561044257827f01000000000000000000000000000000000000000000000000000000000000000286600081518110151561041157fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b8280600101935050610336565b834203975060008814156104d7577fc65844e8ee2558ed559edaad0fdb8d4149b19d5bb4d863bc498bed24f6b2df516040518080602001828103825260088152602001807f546f6f206661737400000000000000000000000000000000000000000000000081525060200191505060405180910390a160008081915080905097509750610537565b876120008a028115156104e657fe5b0496507f2706c3a31ad5099929a565f99a03e84ccc8e03f8acbe063e95fea7a02c24816689898960405180848152602001838152602001828152602001935050505060405180910390a18787975097505b505050505050915091565b60008061054f600161025f565b9150915090915600a165627a7a7230582094a75deef211b53a00f42c1c35eac2443a622a3bf3723f94b574418e2908ca5d0029"};

    public static final String BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", BINARY_ARRAY);

    public static final String[] SM_BINARY_ARRAY = {"608060405234801561001057600080fd5b50610583806100206000396000f300608060405260043610610083576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806315b85196146100885780634b1909d9146100ba57806382befe0e146100ec578063c08200171461011e578063c86fa6af14610150578063e2fc4d6a14610182578063ef703c69146101ca575b600080fd5b34801561009457600080fd5b5061009d6101fc565b604051808381526020018281526020019250505060405180910390f35b3480156100c657600080fd5b506100cf610212565b604051808381526020018281526020019250505060405180910390f35b3480156100f857600080fd5b50610101610227565b604051808381526020018281526020019250505060405180910390f35b34801561012a57600080fd5b50610133610234565b604051808381526020018281526020019250505060405180910390f35b34801561015c57600080fd5b50610165610249565b604051808381526020018281526020019250505060405180910390f35b34801561018e57600080fd5b506101ad6004803603810190808035906020019092919050505061025e565b604051808381526020018281526020019250505060405180910390f35b3480156101d657600080fd5b506101df610541565b604051808381526020018281526020019250505060405180910390f35b60008061020a6101f461025e565b915091509091565b60008061021f606461025e565b915091509091565b6000804342915091509091565b600080610241600a61025e565b915091509091565b600080610256600161025e565b915091509091565b600080606060008060008060006104006040519080825280601f01601f1916602001820160405280156102a05781602001602082028038833980820191505090505b509550600094505b61040085101561032d57610100858115156102bf57fe5b067f01000000000000000000000000000000000000000000000000000000000000000286868151811015156102f057fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a90535084806001019550506102a8565b429350600092505b610400890283101561044e57856040518082805190602001908083835b6020831015156103775780518252602082019150602081019050602083039250610352565b6001836020036101000a038019825116818451168082178552505050505050905001915050604051809103902091507f3a4f9c8b7e6d5a2f1b4c8e9a7d6f5c3b2a1e9f8d7c6b5a4f3e2d1c0b9a8f7e6d6001028218905060006001028160001916141561044157827f01000000000000000000000000000000000000000000000000000000000000000286600081518110151561041057fe5b9060200101907effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff1916908160001a9053505b8280600101935050610335565b834203975060008814156104d6577fd0d2d7330de820ce08790281956a31395825a6a5de33cd7240050f27410536e46040518080602001828103825260088152602001807f546f6f206661737400000000000000000000000000000000000000000000000081525060200191505060405180910390a160008081915080905097509750610536565b876120008a028115156104e557fe5b0496507f8b167b538b77f920d1db629cdbfab41d97794452bd41e4a24f55257a62964e5e89898960405180848152602001838152602001828152602001935050505060405180910390a18787975097505b505050505050915091565b60008061054f61040061025e565b9150915090915600a165627a7a72305820a331bb437021c6914cdfedef18e7f8acd3b72ccecc774d3a3b3ef7927a86678e0029"};

    public static final String SM_BINARY = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", SM_BINARY_ARRAY);

    public static final String[] ABI_ARRAY = {"[{\"constant\":false,\"inputs\":[],\"name\":\"test500MB\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"test1GB\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"info\",\"outputs\":[{\"name\":\"blockNum\",\"type\":\"uint256\"},{\"name\":\"time\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"test10MB\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"test100MB\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"totalMB\",\"type\":\"uint256\"}],\"name\":\"bench\",\"outputs\":[{\"name\":\"sec\",\"type\":\"uint256\"},{\"name\":\"mbps\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"test1MB\",\"outputs\":[{\"name\":\"\",\"type\":\"uint256\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"totalMB\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"elapsedSec\",\"type\":\"uint256\"},{\"indexed\":false,\"name\":\"mbps\",\"type\":\"uint256\"}],\"name\":\"Bench\",\"type\":\"event\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"reason\",\"type\":\"string\"}],\"name\":\"Failed\",\"type\":\"event\"}]"};

    public static final String ABI = org.fisco.bcos.sdk.utils.StringUtils.joinAll("", ABI_ARRAY);

    public static final String FUNC_TEST500MB = "test500MB";

    public static final String FUNC_TEST1GB = "test1GB";

    public static final String FUNC_INFO = "info";

    public static final String FUNC_TEST10MB = "test10MB";

    public static final String FUNC_TEST100MB = "test100MB";

    public static final String FUNC_BENCH = "bench";

    public static final String FUNC_TEST1MB = "test1MB";

    public static final Event BENCH_EVENT = new Event("Bench", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
    ;

    public static final Event FAILED_EVENT = new Event("Failed", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
    ;

    protected SM3EncryptBench(String contractAddress, Client client, CryptoKeyPair credential) {
        super(getBinary(client.getCryptoSuite()), contractAddress, client, credential);
    }

    public static String getBinary(CryptoSuite cryptoSuite) {
        return (cryptoSuite.getCryptoTypeConfig() == CryptoType.ECDSA_TYPE ? BINARY : SM_BINARY);
    }

    public TransactionReceipt test500MB() {
        final Function function = new Function(
                FUNC_TEST500MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] test500MB(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TEST500MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTest500MB() {
        final Function function = new Function(
                FUNC_TEST500MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getTest500MBOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TEST500MB, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public TransactionReceipt test1GB() {
        final Function function = new Function(
                FUNC_TEST1GB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] test1GB(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TEST1GB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTest1GB() {
        final Function function = new Function(
                FUNC_TEST1GB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getTest1GBOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TEST1GB, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public Tuple2<BigInteger, BigInteger> info() throws ContractException {
        final Function function = new Function(FUNC_INFO, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = executeCallWithMultipleValueReturn(function);
        return new Tuple2<BigInteger, BigInteger>(
                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue());
    }

    public TransactionReceipt test10MB() {
        final Function function = new Function(
                FUNC_TEST10MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] test10MB(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TEST10MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTest10MB() {
        final Function function = new Function(
                FUNC_TEST10MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getTest10MBOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TEST10MB, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public TransactionReceipt test100MB() {
        final Function function = new Function(
                FUNC_TEST100MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] test100MB(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TEST100MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTest100MB() {
        final Function function = new Function(
                FUNC_TEST100MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getTest100MBOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TEST100MB, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public TransactionReceipt bench(BigInteger totalMB) {
        final Function function = new Function(
                FUNC_BENCH, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(totalMB)), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] bench(BigInteger totalMB, TransactionCallback callback) {
        final Function function = new Function(
                FUNC_BENCH, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(totalMB)), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForBench(BigInteger totalMB) {
        final Function function = new Function(
                FUNC_BENCH, 
                Arrays.<Type>asList(new org.fisco.bcos.sdk.abi.datatypes.generated.Uint256(totalMB)), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple1<BigInteger> getBenchInput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getInput().substring(10);
        final Function function = new Function(FUNC_BENCH, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple1<BigInteger>(

                (BigInteger) results.get(0).getValue()
                );
    }

    public Tuple2<BigInteger, BigInteger> getBenchOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_BENCH, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public TransactionReceipt test1MB() {
        final Function function = new Function(
                FUNC_TEST1MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeTransaction(function);
    }

    public byte[] test1MB(TransactionCallback callback) {
        final Function function = new Function(
                FUNC_TEST1MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return asyncExecuteTransaction(function, callback);
    }

    public String getSignedTransactionForTest1MB() {
        final Function function = new Function(
                FUNC_TEST1MB, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return createSignedTransaction(function);
    }

    public Tuple2<BigInteger, BigInteger> getTest1MBOutput(TransactionReceipt transactionReceipt) {
        String data = transactionReceipt.getOutput();
        final Function function = new Function(FUNC_TEST1MB, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
        List<Type> results = FunctionReturnDecoder.decode(data, function.getOutputParameters());
        return new Tuple2<BigInteger, BigInteger>(

                (BigInteger) results.get(0).getValue(), 
                (BigInteger) results.get(1).getValue()
                );
    }

    public List<BenchEventResponse> getBenchEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(BENCH_EVENT, transactionReceipt);
        ArrayList<BenchEventResponse> responses = new ArrayList<BenchEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            BenchEventResponse typedResponse = new BenchEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.totalMB = (BigInteger) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse.elapsedSec = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            typedResponse.mbps = (BigInteger) eventValues.getNonIndexedValues().get(2).getValue();
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

    public List<FailedEventResponse> getFailedEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(FAILED_EVENT, transactionReceipt);
        ArrayList<FailedEventResponse> responses = new ArrayList<FailedEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            FailedEventResponse typedResponse = new FailedEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.reason = (String) eventValues.getNonIndexedValues().get(0).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public void subscribeFailedEvent(String fromBlock, String toBlock, List<String> otherTopics, EventCallback callback) {
        String topic0 = eventEncoder.encode(FAILED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,fromBlock,toBlock,otherTopics,callback);
    }

    public void subscribeFailedEvent(EventCallback callback) {
        String topic0 = eventEncoder.encode(FAILED_EVENT);
        subscribeEvent(ABI,BINARY,topic0,callback);
    }

    public static SM3EncryptBench load(String contractAddress, Client client, CryptoKeyPair credential) {
        return new SM3EncryptBench(contractAddress, client, credential);
    }

    public static SM3EncryptBench deploy(Client client, CryptoKeyPair credential) throws ContractException {
        return deploy(SM3EncryptBench.class, client, credential, getBinary(client.getCryptoSuite()), "");
    }

    public static class BenchEventResponse {
        public TransactionReceipt.Logs log;

        public BigInteger totalMB;

        public BigInteger elapsedSec;

        public BigInteger mbps;
    }

    public static class FailedEventResponse {
        public TransactionReceipt.Logs log;

        public String reason;
    }
}
