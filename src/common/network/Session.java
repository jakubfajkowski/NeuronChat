package common.network;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Session {
    private boolean disposed;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private Thread messageReadingThread;

    public Session(Socket socket, LinkedBlockingQueue<ClientMessage> messages) throws IOException {
        this.socket = socket;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        runMessageReadingThread(messages);
    }

    private void runMessageReadingThread(LinkedBlockingQueue<ClientMessage> messages) {
        messageReadingThread = new Thread(() -> {
            while (!disposed) {
                try {
                    ClientMessage message = (ClientMessage) in.readObject();
                    messages.put(message);
                } catch (IOException | InterruptedException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        messageReadingThread.setDaemon(true); // terminate when main ends
        messageReadingThread.start();
    }

    public void write(ClientMessage message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispose() {
        disposed = true;
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public Thread getMessageReadingThread() {
        return messageReadingThread;
    }
}