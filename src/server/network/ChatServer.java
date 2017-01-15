package server.network;

import common.network.*;
import common.util.Log;
import common.util.User;
import common.util.UserCredentials;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class ChatServer extends Server {
    private AuthenticationService authenticationService;

    private Map<User, SessionId> userSessionIdMap;
    private TimerTask broadcastUserListTimerTask;

    public ChatServer(int port) throws IOException {
        super(port);
        authenticationService = new AuthenticationService();
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

        switch (message.getClientMessageMode()) {
            case MESSAGE:
                passMessage(message);
                break;
            case LOGIN:
                login(message);
                break;
            case REGISTER:
                register(message);
                break;
        }
    }

    private void passMessage(ClientMessage message) {
        User user = message.getAddressee();
        SessionId addresseeSessionId = userSessionIdMap.get(user);
        send(addresseeSessionId, message);
    }

    private void login(ClientMessage message) {
        UserCredentials userCredentials = (UserCredentials) message.getAddressee();
        User user = new User(userCredentials.getUsername());
        SessionId sessionId = (SessionId) message.getPayload();

        message.setAddressee(user);
        boolean loginResult = authenticationService.validate(userCredentials);
        message.setPayload(loginResult);

        if (loginResult)
            Log.print("%s - login succeeded", user);
        else
            Log.print("%s - login failed", user);

        send(sessionId, message);
    }

    private void register(ClientMessage message) {
        UserCredentials userCredentials = (UserCredentials) message.getAddressee();
        User user = new User(userCredentials.getUsername());
        SessionId sessionId = (SessionId) message.getPayload();

        message.setAddressee(user);
        boolean registrationResult = authenticationService.register(userCredentials);
        message.setPayload(registrationResult);

        if (registrationResult)
            Log.print("%s - registration succeeded", user);
        else
            Log.print("%s - registration failed", user);

        send(sessionId, message);
    }

    @Override
    public void onSessionDisposed(Session session) {
        userSessionIdMap.values().remove(session.getSessionId());
        finalizeSession(session);
    }
}
