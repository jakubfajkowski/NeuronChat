package common.encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class NeuralNetworkSecurity {
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
}
