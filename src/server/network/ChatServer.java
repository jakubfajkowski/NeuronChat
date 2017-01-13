package server.network;

import common.network.ClientMessage;
import common.network.SessionId;
import common.util.Log;
import common.util.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer extends Server {
    private Map<User, SessionId> userSessionIdMap;

    public ChatServer(int port) throws IOException {
        super(port);
        userSessionIdMap = new HashMap<>();
    }

    @Override
    protected void handleMessage(ClientMessage message) {
        Log.print("Received from " + message);
        SessionId addresseeSessionId = userSessionIdMap.get(message.getAddressee());

        switch (message.getClientMessageMode()) {
            case MESSAGE:
                send(addresseeSessionId, message);
            case AVAILABLE_USERS:
                List<User> users = new ArrayList<>();
                users.addAll(userSessionIdMap.keySet());
                users.remove(message.getAddressee());
                message.setPayload((Serializable) users);
                break;
            case CONNECTION:
                User user = message.getAddressee();
                SessionId sessionId = (SessionId) message.getPayload();
                userSessionIdMap.put(user, sessionId);
                break;
        }

        send(addresseeSessionId, message);
    }
}
