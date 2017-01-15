package server.network;

import client.network.ChatClient;
import client.network.ChatClientListener;
import client.network.Client;
import common.network.ChatMessage;
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
        User user1 = new User("Test1");
        ChatClient chatClient1 = new ChatClient("127.0.0.1", 10000);
        chatClient1.addListener(chatClientListener);
        User user2 = new User("Test2");
        ChatClient chatClient2 = new ChatClient("127.0.0.1", 10000);
        chatClient2.addListener(chatClientListener);
        Thread.sleep(1000);
        chatClient2.sendMessage("HEJKA STULEJKA", user1);
        chatClient1.sendMessage("STULEJKA HEJKA", user2);
        Thread.sleep(1000000);
    }

}