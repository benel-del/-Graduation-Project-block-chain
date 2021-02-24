package security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AES{
    public SecretKeySpec setKey(String skey) throws NoSuchAlgorithmException {
    	byte[] hash = MessageDigest.getInstance("SHA1").digest(skey.getBytes(StandardCharsets.US_ASCII));
    	byte[] key = Arrays.copyOf(hash, 16);
    	return new SecretKeySpec(key, 0, key.length, "AES");
    }

    public byte[] encrypt (String plainText, SecretKeySpec skeySpec) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec);    // ��ȣȭ ��� �ʱ�ȭ
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());    // ��ȣȭ
    
        return byteCipherText;
    }

    public String decrypt (byte[] byteCipherText, SecretKeySpec skeySpec) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, skeySpec);    // ��ȣȭ ��� �ʱ�ȭ
        byte[] bytePlainText = aesCipher.doFinal(byteCipherText);   // ��ȣȭ
    
        return new String(bytePlainText);
    }
    
    public byte[] StrToByte(String str) {
    	return Base64.decodeBase64(str);
    }
    
    public String ByteToStr(byte[] bt) {
    	return Base64.encodeBase64String(bt);
    }

}
