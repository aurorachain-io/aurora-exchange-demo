package org;

import com.aoa.web3j.core.protocol.Web3j;
import com.aoa.web3j.core.protocol.core.DefaultBlockParameter;
import com.aoa.web3j.core.protocol.core.Request;
import com.aoa.web3j.core.protocol.core.methods.response.AOABlock;
import com.aoa.web3j.core.protocol.core.methods.response.Transaction;
import com.aoa.web3j.core.protocol.core.methods.response.TransactionReceipt;
import com.aoa.web3j.core.protocol.http.HttpService;
import com.aoa.web3j.crypto.Action;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yujian    2020/05/22 扫快定时任务demo
 */
@Slf4j
public class ScanBlockDemo {

    private final Web3j web3j = Web3j.build(new HttpService("http://172.16.20.76:8545"));
    private long lastScanBlockNumber = -1;

    // @Scheduled(fixedDelay = 10 * 1000)
    public void scanAoaTransaction() {
        log.info("【Aurora扫块】定时任务开始");
        try {
            BigInteger currentBlockNumber = web3j.aoaBlockNumber().sendAsync().get().getBlockNumber();
            if (currentBlockNumber == null) {
                log.error("【Aurora扫块】未查询到最新块号，定时任务结束");
                return;
            }
            // 有未扫的块
            if (currentBlockNumber.longValue() > lastScanBlockNumber) {
                log.info("【Aurora扫块】扫块开始 startBlockNumber={}|currentBlockNumber={}", lastScanBlockNumber,
                         currentBlockNumber);
                for (long blockNumber = lastScanBlockNumber + 1; blockNumber <= currentBlockNumber.longValue();
                     blockNumber++) {
                    Request<?, AOABlock> emBlockRequest =
                        web3j.aoaGetBlockByNumber(DefaultBlockParameter.valueOf(BigInteger.valueOf(blockNumber)), true);
                    AOABlock.Block block = emBlockRequest.sendAsync().get().getBlock();
                    if (block == null) {
                        log.error("【Aurora扫块】获取区块异常，定时任务结束");
                        return;
                    }
                    int trxSize = block.getTransactions().size();
                    log.info("【Aurora扫块】blockNumber={}|trxSize={}", blockNumber, trxSize);
                    List<AOABlock.TransactionResult> transactions = block.getTransactions();
                    List<TrxDto> saveDbTrxList = new ArrayList<>();
                    for (AOABlock.TransactionResult transactionResult : transactions) {
                        Transaction transaction = (Transaction) (transactionResult.get());
                        // 只扫em转账,其他交易类型忽略
                        if (transaction.getAction() == Action.ORDINARY_TRX.getValue() &&
                            StringUtils.isEmpty(transaction.getAsset())) {
                            String trxId = transaction.getHash();
                            String toAddress = transaction.getTo();
                            // 如果是子地址转账，to则换成子地址
                            if (StringUtils.isNotEmpty(transaction.getSubAddress())) {
                                toAddress = dealSubAddress(transaction.getSubAddress());
                            }

                            Optional<TransactionReceipt> transactionReceipt =
                                web3j.aoaGetTransactionReceipt(trxId).sendAsync().get().getTransactionReceipt();
                            if (!transactionReceipt.isPresent()) {
                                log.error("【Aurora扫块】查询收据异常，定时任务结束");
                                return;
                            }
                            TransactionReceipt trxReceipt = transactionReceipt.get();
                            // 交易状态不是成功
                            if (!"0x1".equals(trxReceipt.getStatus())) {
                                log.error("【Aurora扫块】交易状态失败 trxHash={}|trxReceipt={}", trxReceipt.getTransactionHash(),
                                          trxReceipt);
                                return;
                            }


                            BigInteger gasPrice = transaction.getGasPrice();
                            BigInteger gasUsed = transactionReceipt.get().getGasUsed();
                            BigDecimal gas = new BigDecimal(gasPrice.multiply(gasUsed));
                            BigDecimal amount = new BigDecimal(transaction.getValue());
                            BigInteger txTimestamp = block.getTimestamp().multiply(new BigInteger("1000"));

                            TrxDto trxDto = new TrxDto();
                            trxDto.setTrxId(trxId);
                            trxDto.setFromAddress(transaction.getFrom());
                            trxDto.setToAddress(toAddress);
                            trxDto.setAmount(amount);
                            trxDto.setTrxTime(txTimestamp.longValue());
                            trxDto.setGasPrice(gasPrice);
                            trxDto.setGas(gas);
                            trxDto.setGasUsed(gasUsed);

                            saveDbTrxList.add(trxDto);
                        }
                    }
                    // appBlockService.insert(block) save block TODO 区块存进数据库
                    if (saveDbTrxList.size() > 0) {
                        // appTrxService.insert(saveDbTrxList); // TODO 交易存进数据库
                    }
                }
            }
            lastScanBlockNumber = currentBlockNumber.longValue();

        } catch (Exception e) {
            log.error("【Aurora扫块】定时任务异常", e);
        }

        log.info("【Aurora扫块】定时任务结束");
    }

    private String dealSubAddress(String subAddress) {
        subAddress = subAddress.replace("0x", "AOA");
        subAddress = subAddress.replace("aoa", "AOA");
        return subAddress;
    }

    public static void main(String[] args) throws Exception {
        ScanBlockDemo scanBlockDemo = new ScanBlockDemo();
        while (true) {
            scanBlockDemo.scanAoaTransaction();
            //休眠10秒
            TimeUnit.SECONDS.sleep(10);
        }
    }


}

@Data
class TrxDto {
    private String trxId;
    private String fromAddress;
    private String toAddress;
    private BigDecimal amount;
    private BigInteger gasPrice;
    private BigDecimal gas;
    private BigInteger gasUsed;
    private long trxTime;
}
