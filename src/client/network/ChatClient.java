package client.network;

import common.network.ClientMessage;
import common.network.ClientMessageMode;
import common.network.SessionId;
import common.util.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends Client {
    private User user;
    private List<ChatClientListener> chatClientListeners;

    public ChatClient(String ipAddress, int port, String username) throws IOException {
        super(ipAddress, port);
        this.user = new User(username);
        this.chatClientListeners = new ArrayList<>();
    }

    @Override
    protected void receiveMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case CONNECTION:
                SessionId sessionId = (SessionId) message.getPayload();
                sendUsername(sessionId);
                break;

            default:
                for (ChatClientListener c: chatClientListeners) {
                    c.handleMessage(message);
                }
                break;
        }


    }

    public void addListener(ChatClientListener chatClientListener) {
        chatClientListeners.add(chatClientListener);
    }

    public void sendAvailableUsersRequest() {
        ClientMessage messageToSend = new ClientMessage(
                ClientMessageMode.AVAILABLE_USERS,
                user,
                null
        );
        send(messageToSend);
    }

    public void sendUsername(SessionId sessionId) {
        ClientMessage messageToSend = new ClientMessage(
                ClientMessageMode.CONNECTION,
                user,
                sessionId
        );
        send(messageToSend);
    }
}
