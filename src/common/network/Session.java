package common.network;

import common.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Klasa zawierająca logikę sesji pomiędzy aplikacją kliencką a serwerem
 */
public class Session {
    private SessionId sessionId;
    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean disposed;
    private Thread messageReadingThread;
    private LinkedBlockingQueue<ClientMessage> messages;
    private SessionListener sessionListener;

    public Session(Socket socket, LinkedBlockingQueue<ClientMessage> messages) throws IOException {
        this.socket = socket;
        this.messages = messages;
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        runMessageReadingThread();
    }

    /**
     * Metoda uruchamiająca wątek służący do odbioru wiadomości przez serwer
     */
    private void runMessageReadingThread() {
        messageReadingThread = new Thread(() -> {
            while (!disposed) {
                try {
                    ClientMessage message = (ClientMessage) in.readObject();
                    read(message);
                }
                catch (ClassCastException | StreamCorruptedException ignored) {}
                catch (IOException | InterruptedException | ClassNotFoundException e) {
                    Log.print("Session reading thread exception: " + e.getMessage());
                    dispose();
                }
            }
        });

        messageReadingThread.setDaemon(true); // terminate when main ends
        messageReadingThread.start();
    }

    /**
     * Metoda pobierająca wiadomość z kolejki
     */
    protected void read(ClientMessage message) throws InterruptedException {
        messages.put(message);
    }
    /**
     * Metoda zapisująca wiadomość do kolejki
     */
    public void write(ClientMessage message) {
        try {
            out.writeObject(message);
        } catch (IOException e) {
            Log.print("Session writing exception: " + e.getMessage());
        }
    }

    /**
     * Metoda zrywająca połączenie
     */
    public void dispose() {
        disposed = true;
        try {
            socket.close();
            if (sessionListener != null)
                sessionListener.onSessionDisposed(this);
        } catch (IOException e) {
            Log.print("Session disposing exception: " + e.getMessage());
        }
    }

    public Thread getMessageReadingThread() {
        return messageReadingThread;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public void setSessionId(SessionId sessionId) {
        this.sessionId = sessionId;
    }

    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }
}