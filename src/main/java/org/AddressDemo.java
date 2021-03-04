package org;

import com.aoa.web3j.crypto.Credentials;
import com.aoa.web3j.crypto.ECKeyPair;
import com.aoa.web3j.crypto.Keys;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yujian    2020/05/22 地址相关demo，包括地址生成，地址校验
 */
public class AddressDemo {

    /**
     * 生成aoa地址和对应的私钥,与eth地址私钥兼容，将前缀0x更改为AOA即可
     */
    public static void createAddress()
        throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        Credentials credentials = Credentials.create(ecKeyPair);
        System.err.printf("address:%s\n", credentials.getAddress());
        // 16进制
        System.err.printf("publicKey:%s\n", credentials.getEcKeyPair().getPublicKey().toString(16));
        System.err.printf("privateKey:%s\n", credentials.getEcKeyPair().getPrivateKey().toString(16));

        System.err.printf("publicKey:%s\n", credentials.getEcKeyPair().getPublicKey());
        System.err.printf("privateKey:%s\n", credentials.getEcKeyPair().getPrivateKey());
    }

    /**
     * 通过私钥获取对应的地址
     */
    public static void getAddressByPrivateKey() {
        // 16进制私钥
        String privateKey = "92e8798a13f06adc23fa690410e999b2a9aecb0d4883136b63da643e8a8c5ef2";
        BigInteger b = new BigInteger(privateKey, 16);
        // AOAf12f2e4457f1cdd0ad7c7874e0ff25d5d495b65a
        ECKeyPair ecKeyPair = ECKeyPair.create(b);
        Credentials credentials = Credentials.create(ecKeyPair);
        System.err.printf("address:%s\n", credentials.getAddress());
    }

    private static final Pattern pattern = Pattern.compile("(?i:^aoa|0x)[0-9a-f]{40}[0-9A-Za-z]{0,32}$");
    /**
     * 校验地址的合法性，支持子地址
     * 子地址即正常地址 + 32位md5值，优点是记账方便并免于币归集
     */
    public static boolean verifyAddress(String address) {
        address = address.toLowerCase();
        Matcher matcher = pattern.matcher(address);
        return matcher.find();
    }

    public static void main(String[] args) throws Exception{
        AddressDemo.createAddress();
        AddressDemo.getAddressByPrivateKey();
        System.out.println(AddressDemo.verifyAddress("AOA2e25d6f13330163134b9e321491ca0d45233e054"));
    }


}
