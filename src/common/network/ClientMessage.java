package common.network;

import common.util.User;

import java.io.Serializable;

public class ClientMessage implements Serializable {
    static final long serialVersionUID = 1L;

    private ClientMessageMode clientMessageMode;
    private User addressee;
    private Serializable payload;

    public ClientMessage(ClientMessageMode clientMessageMode, User addressee, Serializable payload) {
        this.clientMessageMode = clientMessageMode;
        this.addressee = addressee;
        this.payload = payload;
    }

    @Override
    public String toString() {
        return addressee + " message " + clientMessageMode;
    }

    public ClientMessageMode getClientMessageMode() {
        return clientMessageMode;
    }

    public User getAddressee() {
        return addressee;
    }

    public Serializable getPayload() {
        return payload;
    }

    public void setPayload(Serializable payload) {
        this.payload = payload;
    }
}
