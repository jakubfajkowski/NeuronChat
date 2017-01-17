package server.network;

import client.network.ChatClient;
import client.network.ChatClientListener;
import common.encryption.LearningParameters;
import common.encryption.LearningRule;
import common.network.ChatMessage;
import common.network.ClientMessage;
import common.util.User;
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
            public void handleClientMessage(ClientMessage message) {
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
        ChatMessage chatMessage1 = new ChatMessage(user2, "HEJKA STULEJKA");
        ChatMessage chatMessage2 = new ChatMessage(user1, "STULEJKA HEJKA");
        chatClient2.sendMessage(chatMessage1, user1);
        chatClient1.sendMessage(chatMessage2, user2);
        Thread.sleep(1000000);
    }

    @Test
    public void encryptionTest() throws Exception {
        ChatServer chatServer = new ChatServer(10000);
        ChatClientListener chatClientListener = new ChatClientListener() {
            @Override
            public void handleClientMessage(ClientMessage message) {
                System.out.println(message.getClientMessageMode() + " " + message.getAddressee());
            }
        };

        Thread.sleep(5000);
        User user1 = new User("Test1");
        ChatClient chatClient1 = new ChatClient("127.0.0.1", 10000);
        chatClient1.addListener(chatClientListener);
        /*User user2 = new User("Test2");
        ChatClient chatClient2 = new ChatClient("127.0.0.1", 10000);
        chatClient2.addListener(chatClientListener);*/
        Thread.sleep(2000);
        LearningParameters lp = new LearningParameters(
                LearningRule.RANDOM_WALK,
                4,
                8,
                4,
                250,
                10000
        );
        chatClient1.sendInitializeKeyNegotiationRequest(lp);
        Thread.sleep(1000000);
    }
}