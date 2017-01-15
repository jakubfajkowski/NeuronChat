package client.network;

import common.network.ClientMessage;
import common.network.Session;
import common.network.SessionId;
import common.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Client {
    private Session serverSession;
    private LinkedBlockingQueue<ClientMessage> messages;
    private Socket socket;
    private Thread messageHandlingThread;
    private boolean running;

    public Client(String ipAddress, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, port));

        messages = new LinkedBlockingQueue<>();
        serverSession = new Session(socket, messages);

        running = true;
        runMessageHandlingThread();
    }

    private void runMessageHandlingThread() {
        messageHandlingThread = new Thread(() -> {
            while (running) {
                try {
                    ClientMessage message = messages.take();
                    receiveMessage(message);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        messageHandlingThread.setDaemon(true);
        messageHandlingThread.start();
    }

    protected abstract void receiveMessage(ClientMessage message);

    protected void send(ClientMessage message) {
        serverSession.write(message);
    }

    public void stop() throws IOException {
        running = false;
        serverSession.dispose();
    }

    protected void setServerSessionId(SessionId sessionId) {
        serverSession.setSessionId(sessionId);
    }

    protected SessionId getServerSessionId() {
        return serverSession.getSessionId();
    }

    public Thread getMessageHandlingThread() {
        return messageHandlingThread;
    }
}