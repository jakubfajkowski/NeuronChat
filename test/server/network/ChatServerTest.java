package server.network;

import client.network.ChatClient;
import client.network.ChatClientListener;
import client.network.Client;
import common.network.ClientMessage;
import common.util.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by FajQa on 12.01.2017.
 */
public class ChatServerTest {
    @Test
    public void handleMessage() throws Exception {
        ChatServer chatServer = new ChatServer(10000);
        ChatClientListener chatClientListener = new ChatClientListener() {
            @Override
            public void handleMessage(ClientMessage message) {
                System.out.println(message.getClientMessageMode() + " " + message.getAddressee());
            }
        };

        Thread.sleep(1000);

        ChatClient chatClient = new ChatClient("127.0.0.1", 10000, "Test User");
        chatClient.addListener(chatClientListener);
        Thread.sleep(1000);
        chatClient.sendAvailableUsersRequest();
        Thread.sleep(1000000);
    }

}