package client.network;

import common.network.ClientMessage;
/**
 * Interfejs zawierający metodę służącą do obsługi wiadomości przychodzących
 */
public interface ChatClientListener {
    void handleClientMessage(ClientMessage message);
}
