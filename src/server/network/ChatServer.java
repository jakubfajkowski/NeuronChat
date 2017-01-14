package server.network;

import common.network.*;
import common.util.Log;
import common.util.User;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ChatServer extends Server {
    private Map<User, SessionId> userSessionIdMap;
    private TimerTask broadcastUserListTimerTask;

    public ChatServer(int port) throws IOException {
        super(port);
        userSessionIdMap = new HashMap<>();

        runBroadcastUserListTimerTask();
    }

    private void runBroadcastUserListTimerTask()
    {
        broadcastUserListTimerTask = new TimerTask() {
            @Override
            public void run() {
                broadcastUserList();
            }
        };

        Timer timer = new Timer();
        timer.schedule(broadcastUserListTimerTask, 0, 5000);
    }

    private void broadcastUserList() {
        ClientMessage message = new ClientMessage(ClientMessageMode.AVAILABLE_USERS, null, null);
        List<User> users = new ArrayList<>();
        users.addAll(userSessionIdMap.keySet());

        users.remove(message.getAddressee());
        message.setPayload((Serializable) users);
        sendToAll(message);
    }

    @Override
    protected void handleMessage(ClientMessage message) {
        Log.print("Received from " + message);
        SessionId addresseeSessionId = userSessionIdMap.get(message.getAddressee());

        switch (message.getClientMessageMode()) {
            case MESSAGE:
                send(addresseeSessionId, message);
                break;
            case CONNECTION:
                User user = message.getAddressee();
                SessionId sessionId = (SessionId) message.getPayload();
                userSessionIdMap.put(user, sessionId);
                break;
        }
    }

    @Override
    public void onSessionDisposed(Session session) {
        userSessionIdMap.values().remove(session.getSessionId());
        finalizeSession(session);
    }
}
