package network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Client {
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
            while(running){
                try{
                    ClientMessage message = messages.take();
                    handleMessage(message);
                }
                catch(InterruptedException e){ e.printStackTrace(); }
            }
        });

        messageHandlingThread.setDaemon(true);
        messageHandlingThread.start();
    }

    private void handleMessage(ClientMessage message) {

    }

    public void send(ClientMessage message) {
        serverSession.write(message);
    }

    public void stop() throws IOException {
        running = false;
        serverSession.dispose();
    }

    public Thread getMessageHandlingThread() {
        return messageHandlingThread;
    }
}