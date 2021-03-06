package common.network;

import common.encryption.Security;
import common.util.User;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
/**
 * Klasa opisująca, wysyłająca i dekodująca wiadomości wszelkiego typu wysyłanych do serwera
 **/
public class ClientMessage implements Serializable {
    static final long serialVersionUID = 1L;

    private ClientMessageMode clientMessageMode;

    private Serializable addressee;
    private Serializable payload;

    public ClientMessage(ClientMessageMode clientMessageMode, User addressee, Serializable payload) {
        this.clientMessageMode = clientMessageMode;
        this.addressee = addressee;
        this.payload = payload;
    }

    public void encryptPayload(byte[] key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        payload = Security.encryptObject(payload, key);
    }

    public void decryptPayload(byte[] key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        payload = Security.decryptObject((byte[]) payload, key);
    }

    public void encryptAddressee(byte[] key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        addressee = Security.encryptObject(addressee, key);
    }

    public void decryptAddressee(byte[] key) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException {
        addressee = Security.decryptObject((byte[]) addressee, key);
    }

    @Override
    public String toString() {
        return addressee + " message " + clientMessageMode;
    }

    public void setClientMessageMode(ClientMessageMode clientMessageMode) {
        this.clientMessageMode = clientMessageMode;
    }

    public ClientMessageMode getClientMessageMode() {
        return clientMessageMode;
    }

    public Serializable getAddressee() {
        return addressee;
    }

    public void setAddressee(User addressee) {
        this.addressee = addressee;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }
}
