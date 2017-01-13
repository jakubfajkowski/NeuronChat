package client.network;

import common.network.ChatMessage;
import common.network.ClientMessage;
import common.network.ClientMessageMode;
import common.network.SessionId;
import common.util.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends Client {
    private User localUser;
    private List<ChatClientListener> chatClientListeners;

    public ChatClient(String ipAddress, int port, User localUser) throws IOException {
        super(ipAddress, port);
        this.localUser = localUser;
        this.chatClientListeners = new ArrayList<>();
    }

    @Override
    protected void receiveMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case CONNECTION:
                SessionId sessionId = (SessionId) message.getPayload();
                sendUsername(sessionId);
                sendAvailableUsersRequest();
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
                localUser,
                null
        );
        send(messageToSend);
    }

    private void sendUsername(SessionId sessionId) {
        ClientMessage messageToSend = new ClientMessage(
                ClientMessageMode.CONNECTION,
                localUser,
                sessionId
        );
        send(messageToSend);
    }

    public void sendMessage(String chatMessageText, User addressee) {
        ClientMessage messageToSend = new ClientMessage(
                ClientMessageMode.MESSAGE,
                addressee,
                new ChatMessage(localUser, chatMessageText)
        );
        send(messageToSend);
    }
}
