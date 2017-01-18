package server.network;

import common.network.*;
import common.util.Log;
import common.util.User;
import common.util.UserCredentials;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
/**
 * Klasa która zawiera metody pozwalające wykonywać serwerowi aplikacji podstawowe zadania
 * takie jak wysyłanie wiadomości, nasłuchiwanie na przyjście wiadomości, ich odbieranie,
 * odpowiednią obsługę, czy logowania i rejestrowania użytkowników
 */
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

    /**
     * Metoda uruchamiająca timer odpowiedzialny za cykliczne wysyłanie userom
     * aktualizacji listy dostępnych użytkoników
     */
    private void runBroadcastUserListTimerTask() {
        broadcastUserListTimerTask = new TimerTask() {
            @Override
            public void run() {
                broadcastUserList();
            }
        };

        Timer timer = new Timer();
        timer.schedule(broadcastUserListTimerTask, 0, 5000);
    }

    /**
     * Metoda wysyłająca do wszystkich użytkoników aktualizację tablicy
     * dostępnych użytkowników
     */
    private void broadcastUserList() {
        ClientMessage message = new ClientMessage(ClientMessageMode.AVAILABLE_USERS, null, null);
        ArrayList<User> users = new ArrayList<>();
        users.addAll(userSessionIdMap.keySet());

        message.setPayload(users);
        sendToAll(message);
    }

    /**
     * Metoda wysyłająca wiadomość do wszystkich użytkowników
     */
    private void sendToAll(ClientMessage message) {
        for (SessionId s: userSessionIdMap.values()) {
            send(s, message);
            //Log.print("Broadcast %s to %d user(s)", message.getClientMessageMode(), userSessionIdMap.size());
        }
    }

    /**
     * Metoda odpowiadająca za obsługę wiadomości przychodzących do serwera
     */
    @Override
    protected void handleMessage(ClientMessage message) {
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

    /**
     * Metoda przekazująca wiadomość z serwera do użytkownika docelowego
     */
    private void passMessage(ClientMessage message) {
        User user = (User) message.getAddressee();
        SessionId addresseeSessionId = userSessionIdMap.get(user);
        Log.print("Sent to " + message);
        send(addresseeSessionId, message);
    }

    /**
     * Metoda odpowiadająca za obsługę logowania użytkowników
     */
    private void login(ClientMessage message) {
        UserCredentials userCredentials = (UserCredentials) message.getAddressee();
        User user = new User(userCredentials.getUsername());
        SessionId sessionId = (SessionId) message.getPayload();

        message.setAddressee(user);
        boolean loginResult = authenticationService.validate(userCredentials);
        message.setPayload(loginResult);

        if (loginResult) {
            userSessionIdMap.put(user, sessionId);
            Log.print("%s - login succeeded", user);
        }
        else {
            Log.print("%s - login failed", user);
        }

        Log.print("Sent to " + message);
        send(sessionId, message);
    }

    /**
     * Metoda odpowiadająca za obsługę rejestrowania nowych użytkowników
     */
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

        Log.print("Sent to " + message);
        send(sessionId, message);
    }

    /**
     * Metoda odpowiadająca za reakcję gdy połączenie serwer-klient zostanie zerwane
     */
    @Override
    public void onSessionDisposed(Session session) {
        userSessionIdMap.values().remove(session.getSessionId());
        finalizeSession(session);
    }
}
