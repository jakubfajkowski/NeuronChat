package server;

import server.network.ChatServer;

public class Main {
    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer(10000);
            server.getConnectThread().join();
        }
        catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
