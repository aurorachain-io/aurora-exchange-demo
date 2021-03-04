package org;

import com.aoa.web3j.core.protocol.Web3j;
import com.aoa.web3j.core.protocol.core.DefaultBlockParameter;
import com.aoa.web3j.core.protocol.core.Request;
import com.aoa.web3j.core.protocol.core.methods.response.AOABlock;
import com.aoa.web3j.core.protocol.core.methods.response.AOABlockNumber;
import com.aoa.web3j.core.protocol.core.methods.response.AOAGetTransactionReceipt;
import com.aoa.web3j.core.protocol.core.methods.response.AOATransaction;
import com.aoa.web3j.core.protocol.core.methods.response.Transaction;
import com.aoa.web3j.core.protocol.core.methods.response.TransactionReceipt;
import com.aoa.web3j.core.protocol.http.HttpService;

import java.math.BigInteger;
import java.util.concurrent.ExecutionException;

/**
 * @author yujian    2020/05/22 查询区块信息，交易信息demo
 */
public class QueryBlockAndTrxDemo {

    private static final Web3j web3j = Web3j.build(new HttpService("http://172.16.20.76:8545"));


    /**
     * 查询当前节点最新块高度
     *
     * @return 区块高度
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static BigInteger queryCurrentBlock() throws ExecutionException, InterruptedException {
        Request<?, AOABlockNumber> emBlockNumberRequest = web3j.aoaBlockNumber();
        AOABlockNumber result = emBlockNumberRequest.sendAsync().get();
        return result.getBlockNumber();
    }

    /**
     * 查询某个块的区块信息
     *
     * @param blockNumber 区块高度
     * @return 该块信息
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static AOABlock.Block getBlockByNumber(long blockNumber) throws ExecutionException, InterruptedException {
        Request<?, AOABlock> emBlockRequest =
            web3j.aoaGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), true);
        AOABlock emBlock = emBlockRequest.sendAsync().get();
        return emBlock.getBlock();
    }

    /**
     * 通过交易hash查询某笔交易
     *
     * @param trxId 交易hash
     * @return 该笔交易的具体信息
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Transaction getTransactionByTrxId(String trxId) throws ExecutionException, InterruptedException {
        Request<?, AOATransaction> request = web3j.aoaGetTransactionByHash(trxId);
        return request.sendAsync().get().getResult();
    }

    /**
     * 通过交易hash查询某笔交易的收据
     *
     * @param trxId 交易hash
     * @return 交易收据的具体信息
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static TransactionReceipt getTransactionReceiptByTrxId(String trxId)
        throws ExecutionException, InterruptedException {
        Request<?, AOAGetTransactionReceipt> request = web3j.aoaGetTransactionReceipt(trxId);
        return request.sendAsync().get().getResult();
    }


    public static void main(String[] args) throws Exception {
        queryCurrentBlock();
        getBlockByNumber(2L);
        String trxId = "0xd295457a7004b45f12b459405b91cff179613aafa0df7f52279d602edf0563c2";
        getTransactionByTrxId(trxId);
        getTransactionReceiptByTrxId(trxId);
    }
}
