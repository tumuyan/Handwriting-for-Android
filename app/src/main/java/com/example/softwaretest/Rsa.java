package com.example.softwaretest;

import android.util.Base64;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class Rsa {
    static final String RSA = "RSA";

    private KeyPair keyPair;
    private KeyFactory keyFactory;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Rsa() {
        this(null);
    }

    public Rsa(Object key) {

        try {
            keyFactory = KeyFactory.getInstance(RSA);

            if (key instanceof PublicKey) {
                publicKey = (PublicKey) key;
            } else if (key instanceof PrivateKey) {
                privateKey = (PrivateKey) key;
            } else {
                keyPair = generateSenderPublicKey();
                RSAPublicKey rsaPublicKey = (RSAPublicKey) keyPair.getPublic();
                RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) keyPair.getPrivate();

                PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(rsaPrivateKey.getEncoded());
                privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

                X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(rsaPublicKey.getEncoded());
                publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    // 私钥加密，输出base64编码的字符串
    public String privateEncode(String str) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            byte[] result = cipher.doFinal(str.getBytes());
            String base64 = Base64.encodeToString(result, Base64.DEFAULT);
            System.out.println("私钥加密：" + base64);
            return base64;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 公钥加密，输出base64编码的字符串
    public String publicEncode(String str) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] result = cipher.doFinal(str.getBytes());
            String base64 = Base64.encodeToString(result, Base64.DEFAULT);
            System.out.println("公钥加密：" + base64);
            return base64;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //  公钥解密
    public String publicDecode(String base64str) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            byte[] result = cipher.doFinal(Base64.decode(base64str, Base64.DEFAULT));
            String s = new String(result);
            System.out.println("公钥解密：" + s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    //  私钥解密
    public String privateDecode(String base64str) {
        try {
            Cipher cipher = Cipher.getInstance(RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] result = cipher.doFinal(Base64.decode(base64str, Base64.DEFAULT));
            String s = new String(result);
            System.out.println("私钥解密：" + s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 构建密钥对
     *
     * @return 构建完的公钥私钥
     * @throws NoSuchAlgorithmException
     */
    private static KeyPair generateSenderPublicKey() throws NoSuchAlgorithmException {
        KeyPairGenerator senderKeyPairGenerator = KeyPairGenerator.getInstance(RSA);
        senderKeyPairGenerator.initialize(512);
        return senderKeyPairGenerator.generateKeyPair();
    }
}