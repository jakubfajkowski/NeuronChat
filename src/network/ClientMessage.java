package network;

import java.io.Serializable;

public class ClientMessage implements Serializable {
    static final long serialVersionUID = 1L;

    private ClientMessageMode clientMessageMode;
    private Serializable payload;

    public ClientMessage(ClientMessageMode clientMessageMode, Serializable payload) {
        this.clientMessageMode = clientMessageMode;
        this.payload = payload;
    }

    public ClientMessageMode getClientMessageMode() {
        return clientMessageMode;
    }

    public Serializable getPayload() {
        return payload;
    }
}
