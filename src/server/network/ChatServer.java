package server.network;

import common.network.ClientMessage;
import common.network.SessionId;
import common.util.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ChatServer extends Server {
    private Map<User, SessionId> userSessionIdMap;

    public ChatServer(int port) throws IOException {
        super(port);
        userSessionIdMap = new HashMap<>();
    }

    @Override
    protected void handleMessage(ClientMessage message) {
        SessionId addresseeSessionId = userSessionIdMap.get(message.getAddressee());

        switch (message.getClientMessageMode()) {
            case AVAILABLE_USERS:
                message.setPayload((Serializable) userSessionIdMap.values());
                break;
            case CONNECTION:

        }

        send(addresseeSessionId, message);
    }
}
