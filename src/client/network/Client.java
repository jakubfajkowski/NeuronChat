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

    public SecureSession getServerSession() {
        return serverSession;
    }

    public Thread getMessageHandlingThread() {
        return messageHandlingThread;
    }
}