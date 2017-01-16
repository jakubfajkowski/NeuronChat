package common.encryption;

import org.apache.commons.lang3.SerializationUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class Security {
    private final static String ENCRYPTION_METHOD = "AES";

    public static byte[] encrypt(byte[] input, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] output;
        Cipher cipher = Cipher.getInstance(ENCRYPTION_METHOD);
        SecretKeySpec k = new SecretKeySpec(key, ENCRYPTION_METHOD);
        cipher.init(Cipher.ENCRYPT_MODE, k);
        output = cipher.doFinal(input);
        return output;
    }

    public static byte[] decrypt(byte[] input, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] output;
        Cipher cipher = Cipher.getInstance(ENCRYPTION_METHOD);
        SecretKeySpec k = new SecretKeySpec(key, ENCRYPTION_METHOD);
        cipher.init(Cipher.DECRYPT_MODE, k);
        output = cipher.doFinal(input);
        return output;
    }

    public static byte[] encryptObject(Serializable object, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] objectBytes = SerializationUtils.serialize(object);
        return encrypt(objectBytes, key);
    }

    public static Serializable decryptObject(byte[] encryptedBytes, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] objectBytes = decrypt(encryptedBytes, key);
        return SerializationUtils.deserialize(objectBytes);
    }
}
