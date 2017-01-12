package server.network;

import common.network.ClientMessage;
import common.network.ClientMessageMode;
import common.network.Session;
import common.network.SessionId;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Server {
    private Map<SessionId, Session> sessionMap;
    private LinkedBlockingQueue<ClientMessage> messages;
    private ServerSocket serverSocket;
    private Thread connectThread;
    private boolean running;

    public Server(int port) throws IOException {
        sessionMap = new HashMap<>();

        messages = new LinkedBlockingQueue<>();
        serverSocket = new ServerSocket(port);

        running = true;
        runConnectThread();
        runMessageHandlingThread();
    }

    private void runConnectThread() {
        connectThread = new Thread(() -> {
            while(running){
                try{
                    Socket s = serverSocket.accept();

                    Session session = new Session(s, messages);
                    SessionId sessionId = new SessionId();
                    sessionMap.put(sessionId, session);

                    session.write(new ClientMessage(ClientMessageMode.CONNECTION, null, sessionId));
                }
                catch(IOException e){ e.printStackTrace(); }
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
                catch(InterruptedException e){ e.printStackTrace(); }
            }
        });

        messageHandling.setDaemon(true);
        messageHandling.start();
    }

    protected abstract void handleMessage(ClientMessage message);

    protected void send(SessionId sessionId, ClientMessage message) {
        Session session = sessionMap.get(sessionId);

        if (session != null) {
            session.write(message);
        }
    }

    protected void sendToAll(ClientMessage clientMessage) {
        for (Session s: sessionMap.values()) {
            s.write(clientMessage);
        }
    }

    public void stop() {
        running = false;

        for (Session s: sessionMap.values()) {
            s.dispose();
        }

        messages.clear();
    }

    public Thread getConnectThread() {
        return connectThread;
    }
}