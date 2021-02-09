package security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.KeyGenerator;

public class AES{
    private SecretKeySpec skeySpec;

    public void setKey() throws NoSuchAlgorithmException {
        KeyGenerator generator = KeyGenerator.getInstance("AES");   // AES Key Generator 객체 생성
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        generator.init(128, random);    // AES Key size 지정
        SecretKey secKey = generator.generateKey();    // AES 암호화 알고리즘에서 사용할 대칭키 생성
        skeySpec = new SecretKeySpec(secKey.getEncoded(), "AES");
    }
    
    public static SecretKeySpec setKey(String skey) {
    	byte[] key = StrToByte(skey);
    	return new SecretKeySpec(key, 0, key.length, "AES");
    }

    public String getKey_st(){
        return Base64.encodeBase64String(skeySpec.getEncoded());
    }
    
    public static String getKey_st(SecretKeySpec key){
        return Base64.encodeBase64String(key.getEncoded());
    }
    
    public SecretKeySpec getKey(){
        return skeySpec;
    }

    public static byte[] encrypt (String plainText, SecretKeySpec skeySpec) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, skeySpec);    // 암호화 모드 초기화
        byte[] byteCipherText = aesCipher.doFinal(plainText.getBytes());    // 암호화
    
        return byteCipherText;
    }

    public static String decrypt (byte[] byteCipherText, SecretKeySpec skeySpec) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, skeySpec);    // 복호화 모드 초기화
        byte[] bytePlainText = aesCipher.doFinal(byteCipherText);   // 복호화
    
        return new String(bytePlainText);
    }
    
    public static byte[] StrToByte(String str) {
    	return Base64.decodeBase64(str);
    }
    
    public static String ByteToStr(byte[] bt) {
    	return Base64.encodeBase64String(bt);
    }
    
    public static String recieve(String data, String key) throws Exception {
    	String str[] = extract(data);
    	String message = decrypt(StrToByte(str[0]), AES.setKey(key));
    	if((message + key).hashCode() == Integer.parseInt(str[1]))
    		return message;
    	else
    		return "Error:: data integrity";
    }
    
    private static String[] extract(String st){
		int idx = st.indexOf("||");
        if(idx == -1)
            return null;
        String str[] = new String[2];
        str[0] = st.substring(0, idx);
        str[1] = st.substring(idx + 2);
        return str;
    }
}
