package server;

import server.network.ChatServer;

public class Main {
    public static void main(String[] args) {
        try {
            ChatServer server = new ChatServer(Integer.parseInt(args[0]));
        }
        catch (Exception e) {
            System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
