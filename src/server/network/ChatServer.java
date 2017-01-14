package server.network;

import common.network.*;
import common.util.Log;
import common.util.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

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
            //case AVAILABLE_USERS:
            //    List<User> users = new ArrayList<>();
            //    users.addAll(userSessionIdMap.keySet());
            //    users.remove(message.getAddressee());
            //    message.setPayload((Serializable) users);
            //    break;
            case CONNECTION:
                User user = message.getAddressee();
                SessionId sessionId = (SessionId) message.getPayload();
                userSessionIdMap.put(user, sessionId);
                break;
        }
        send(addresseeSessionId, message);
    }

    @Override
    protected void handleUserList()
    {
        ClientMessage message = new ClientMessage(ClientMessageMode.AVAILABLE_USERS, null, null);
        List<User> users = new ArrayList<>();
        try{
            users.addAll(userSessionIdMap.keySet());
        }
        catch (NullPointerException e) {
            Log.print("User Handle List handling thread exception: " + e.getMessage());
        }
        users.remove(message.getAddressee());
        message.setPayload((Serializable) users);
        sendToAll(message);
    }

    @Override
    public void onSessionDisposed(Session session) {
        userSessionIdMap.values().remove(session.getSessionId());
        finalizeSession(session);
    }
}
