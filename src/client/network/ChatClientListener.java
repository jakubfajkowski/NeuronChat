package client.network;

import common.network.ClientMessage;

public interface ChatClientListener {
    void handleMessage(ClientMessage message);
}
