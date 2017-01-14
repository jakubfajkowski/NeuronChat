package server.network;

import common.network.*;
import common.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Server implements SessionListener {
    private List<Session> sessions;
    private LinkedBlockingQueue<ClientMessage> messages;
    private ServerSocket serverSocket;
    private Thread connectThread;
    private boolean running;
    private TimerTask timerTask;

    public Server(int port) throws IOException {
        sessions = new ArrayList<>();
        messages = new LinkedBlockingQueue<>();
        serverSocket = new ServerSocket(port);

        timerTask = new TimerTask() {
            @Override
            public void run() {
                handleUserList();
            }
        };

        running = true;
        runConnectThread();
        runMessageHandlingThread();
        runUserListHandlingThread(timerTask);

        Log.print("Server started on port: " + port);
    }

    private void runConnectThread() {
        connectThread = new Thread(() -> {
            while(running){
                try{
                    Socket s = serverSocket.accept();

                    Session session = new Session(s, messages);
                    SessionId sessionId = new SessionId();
                    session.setSessionId(sessionId);
                    session.setSessionListener(this);
                    sessions.add(session);
                    Log.print(String.format("Session %s initialized", sessionId));

                    session.write(new ClientMessage(ClientMessageMode.CONNECTION, null, sessionId));
                }
                catch(IOException e){
                    Log.print("Connect socket closed");
                }
            }
        });

        connectThread.setDaemon(true);
        connectThread.start();
    }

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

    private void runUserListHandlingThread(TimerTask tt)
    {
        Thread userListHandling = new Thread(() -> {
            Timer timer = new Timer();
            timer.schedule(tt, 0, 5000);
        });

        userListHandling.setDaemon(true);
        userListHandling.start();
    }

    protected abstract void handleUserList();
    abstract void handleMessage(ClientMessage message);

    void send(SessionId sessionId, ClientMessage message) {
        Optional<Session> session = getSessionBySessionId(sessionId);

        if (session.isPresent()) {
            session.get().write(message);
            Log.print("Sent to " + message);
        }
    }

    protected void sendToAll(ClientMessage message) {
        for (Session s: sessions) {
            s.write(message);
            Log.print("Broadcast " + message);
        }
    }

    public void stop() {
        running = false;

        for (Session s: sessions) {
            s.dispose();
        }

        messages.clear();
    }

    private Optional<Session> getSessionBySessionId(SessionId sessionId) {
        return sessions.stream().filter(session -> session.getSessionId().equals(sessionId)).findFirst();
    }

    protected void finalizeSession(Session session) {
        sessions.remove(session);
        Log.print(String.format("Session %s disposed", session.getSessionId()));
    }

    public Thread getConnectThread() {
        return connectThread;
    }
}