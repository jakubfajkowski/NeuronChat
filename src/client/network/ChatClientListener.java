package client.network;

import common.network.ClientMessage;

public interface ChatClientListener {
    void handleClientMessage(ClientMessage message);
}
