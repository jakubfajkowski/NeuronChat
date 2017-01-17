package client.network;

import common.encryption.TreeParityMachine;
import common.network.ClientMessage;
import common.network.SecureSession;
import common.network.Session;
import common.network.SessionId;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Klasa odpowiadająca za operacje wykonywane z poziomu operacji klienckiej
 */
public abstract class Client {
    private SecureSession serverSession;
    private LinkedBlockingQueue<ClientMessage> messages;
    private Socket socket;
    private Thread messageHandlingThread;
    private boolean running;

    public Client(String ipAddress, int port) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(ipAddress, port));

        messages = new LinkedBlockingQueue<>();
        serverSession = new SecureSession(socket, messages);

        running = true;
        runMessageHandlingThread();
    }

    /**
     * Metoda odpowiadająca za uruchamianie wątku odpowiadającego za obsługę wiadomości przychodzących
     */
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

    /**
     * Metoda odpowiadająca za wysyłanie wiadomości
     */
    protected void send(ClientMessage message) {
        serverSession.write(message);
    }

    /**
     * Metoda odpowiadająca za zatrzymywanie trwającej sesji
     */
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

    public TreeParityMachine getTreeParityMachine() {
        return serverSession.getTreeParityMachine();
    }
}