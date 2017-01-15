package client.network;

import common.network.ChatMessage;
import common.network.ClientMessage;
import common.network.ClientMessageMode;
import common.network.SessionId;
import common.util.User;
import common.util.UserCredentials;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends Client {
    private User localUser;
    private List<ChatClientListener> chatClientListeners;

    public ChatClient(String ipAddress, int port) throws IOException {
        super(ipAddress, port);
        this.chatClientListeners = new ArrayList<>();
    }

    @Override
    protected void receiveMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case INITIALIZE_SESSION:
                setServerSessionId((SessionId) message.getPayload());
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

    public void sendLoginRequest(UserCredentials userCredentials) {
        ClientMessage messageToSend = new ClientMessage(
                ClientMessageMode.LOGIN,
                userCredentials,
                getServerSessionId()
        );
        send(messageToSend);
    }

    public void sendRegisterRequest(UserCredentials userCredentials) {
        ClientMessage messageToSend = new ClientMessage(
                ClientMessageMode.REGISTER,
                userCredentials,
                getServerSessionId()
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

    public User getLocalUser() {
        return localUser;
    }

    public void setLocalUser(User localUser) {
        this.localUser = localUser;
    }
}
