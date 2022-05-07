package org;

import com.aoa.web3j.core.tx.ChainId;
import com.aoa.web3j.core.utils.AOAGas;
import com.aoa.web3j.crypto.Credentials;
import com.aoa.web3j.crypto.RawTransaction;
import com.aoa.web3j.crypto.TransactionDecoder;
import com.aoa.web3j.crypto.TransactionEncoder;
import com.aoa.web3j.utils.Convert;
import com.aoa.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * @author yujian    2020/05/22 离线签名交易相关demo
 */
public class RawTransactionDemo {

    private final static String testAddress = "AOA2e25d6f13330163134b9e321491ca0d45233e054";
    private final static String testPrivateKey = "b968d08d67b3664bebd6b11b517557a29186e3c5f8474959d5db7cc0442b1831";

    public static void main(String[] args) {
        String to = "AOAd9f5038ca3908212d5a13c3b48a4df7c1dfd5a54";
        BigInteger nonce = BigInteger.valueOf(0);
        System.out.println(to.length());
        String result = RawTransactionDemo.signAOATransaction(testAddress, to, testPrivateKey, nonce, "0.01");
        System.out.println(result);
    }

    /**
     * em主链币转账，离线签名
     *
     * @param from             转出地址
     * @param to               转入地址，如 AOAf12f2e4457f1cdd0ad7c7874e0ff25d5d495b65a
     * @param privateKey       转出地址16进制的私钥
     * @param fromAddressNonce 转出地址nonce值
     * @param aoaAmount         aoa的转账数量
     * @return 转账的离线签名字符串
     */
    public static String signAOATransaction(String from, String to, String privateKey, BigInteger fromAddressNonce,
                                            String aoaAmount) {
        BigInteger gas = AOAGas.defaultTrxGas.toBigInteger();
        BigInteger gasPrice = AOAGas.defaultGasPrice.toBigInteger();
        Credentials credentials = Credentials.create(privateKey);

        BigInteger value = Convert.toWei(aoaAmount, Convert.Unit.AOA).toBigInteger();

        RawTransaction rawTransaction = RawTransaction.createAOATransaction(fromAddressNonce, gasPrice, gas, to, value);

        byte chainId = 2;
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
        return Numeric.toHexString(signedMessage);

//        AOASendTransaction aoaSendTransaction = web3j.aoaSendRawTransaction(hexValue).sendAsync().get();
//        if (aoaSendTransaction.getError() != null) {
//            System.err.printf("sendRawTransaction error:%s\n", aoaSendTransaction.getError().getMessage());
//        } else {
//            String transactionHash = aoaSendTransaction.getTransactionHash();
//            System.err.printf("sendRawTransaction success,trxHash:%s\n", transactionHash);
//        }
    }

    /**
     * em主链币转账，离线签名
     *
     * @param from             转出地址
     * @param to               转入地址,地址格式为子地址，如
     *                         AOA8ea2354ba012628dd1dad9e44500a70075664a16202cb962ac59075b964b07152d234b70
     * @param privateKey       转出地址16进制的私钥
     * @param fromAddressNonce 转出地址nonce值
     * @param aoaAmount         aoa的转账数量
     * @return 转账的离线签名字符串
     */
    public static String signAOATransactionWithSubAddress(String from, String to, String privateKey,
                                                          BigInteger fromAddressNonce,
                                                          String aoaAmount) {
        return signAOATransaction(from, to, privateKey, fromAddressNonce, aoaAmount);
    }

    /**
     * 离线签名串反解
     *
     * @param hexValue 离线签名串
     * @return 解析出来的签名信息
     */
    public static RawTransaction decodeRawSign(String hexValue) {
        return TransactionDecoder.decode(hexValue);
    }
}
