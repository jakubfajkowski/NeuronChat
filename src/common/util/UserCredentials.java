package common.util;

import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
/**
 * Klasa przechowująca dane logowania użytkownika - login i hasłó
 */
public class UserCredentials extends User implements Serializable {
    static final long serialVersionUID = 1L;
    private static final String HASH_FUNCTION = "SHA-256";

    private byte[] passwordHash;

    public UserCredentials(String username, String password){
        super(username);

        try {
            this.passwordHash = MessageDigest.getInstance(HASH_FUNCTION).digest(password.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public String getPasswordHashString() {
        return DatatypeConverter.printHexBinary(passwordHash);
    }
}