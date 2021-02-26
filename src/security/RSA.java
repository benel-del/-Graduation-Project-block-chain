package security;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;

public class RSA {
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public void setKey() throws NoSuchAlgorithmException, InvalidKeySpecException{
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.genKeyPair();
		this.publicKey = keyPair.getPublic();
		this.privateKey = keyPair.getPrivate();
	}

	 public String encrypt(String data, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] bCipher = cipher.doFinal(data.getBytes());
		String sCipherBase64 = Base64.encodeBase64String(bCipher);
        return sCipherBase64;
    }

	public String decrypt(String data, Key key) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA");
		byte[] bCipher = Base64.decodeBase64(data.getBytes());
		cipher.init(Cipher.DECRYPT_MODE, key);
		byte[] bPlain = cipher.doFinal(bCipher);
		return new String(bPlain);

    } 

	public PublicKey getPublicKey() {
		return publicKey;
	}
	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey(String publicKey) throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        byte[] decodedKey = Base64.decodeBase64(publicKey.getBytes());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(keySpec);
    }

	public String KeyToStr(Key key) {
		return new String(Base64.encodeBase64(key.getEncoded()));
	}
}