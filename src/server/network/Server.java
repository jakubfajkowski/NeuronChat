package server.network;

import common.network.*;
import common.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Klasa odpowiadająca za operacje wykonywane z poziomu aplikacji serwera
 */
@SuppressWarnings("WeakerAccess")
public abstract class Server implements SessionListener {
    private List<SecureSession> sessions;
    private LinkedBlockingQueue<ClientMessage> messages;
    private ServerSocket serverSocket;
    private Thread connectThread;
    private boolean running;

    public Server(int port) throws IOException {
        sessions = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();
        serverSocket = new ServerSocket(port);

        running = true;
        runConnectThread();
        runMessageHandlingThread();

        Log.print("Server started on port: " + port);
    }

    /**
     * Metoda odpowiadająca za uruchomienie wątku obsługującego tworzenie nowych sesji
     */
    private void runConnectThread() {
        connectThread = new Thread(() -> {
            while(running){
                try{
                    Socket s = serverSocket.accept();

                    SecureSession session = new SecureSession(s, messages);
                    SessionId sessionId = new SessionId();
                    session.setSessionId(sessionId);
                    session.setSessionListener(this);
                    sessions.add(session);
                    Log.print("Session %s initialized", sessionId);

                    session.write(new ClientMessage(ClientMessageMode.INITIALIZE_SESSION, null, sessionId));
                }
                catch(IOException e){
                    Log.print("Connect socket closed");
                }
            }
        });

        connectThread.setDaemon(true);
        connectThread.start();
    }

    /**
     * Metoda odpowiadająca za uruchomienie wątku obsługującego tworzenie nowych sesji
     */
    private void runMessageHandlingThread() {
        Thread messageHandling = new Thread(() -> {
            while(running){
                try{
                    ClientMessage message = messages.take();
                    handleMessage(message);
                }
                catch(InterruptedException e){
                    Log.print("Message handling thread exception: " + e.getMessage());
                }
            }
        });

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    abstract void handleMessage(ClientMessage message);

    /**
     * Metoda odpowiadająca za wysyłanie wiadomości do odpowiedniego klienta
     */
    void send(SessionId sessionId, ClientMessage message) {
        Optional<SecureSession> session = getSessionBySessionId(sessionId);

        if (session.isPresent()) {
            session.get().write(message);
        }
    }

    public void stop() {
        running = false;

        for (Session s: sessions) {
            s.dispose();
        }

        messages.clear();
    }

    private Optional<SecureSession> getSessionBySessionId(SessionId sessionId) {
        return sessions.stream().filter(session -> session.getSessionId().equals(sessionId)).findFirst();
    }

    protected void finalizeSession(Session session) {
        sessions.remove(session);
        Log.print("Session %s disposed", session.getSessionId());
    }

    public Thread getConnectThread() {
        return connectThread;
    }
}