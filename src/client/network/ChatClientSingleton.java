package client.network;

import client.alert.ConnectionAlert;
import common.util.PropertiesManager;

import java.io.IOException;

public class ChatClientSingleton {
    private ChatClient client;

    private static ChatClientSingleton ourInstance = new ChatClientSingleton();

    public static ChatClientSingleton getInstance() {
        return ourInstance;
    }

    private ChatClientSingleton() {
        connectToServer();
    }

    public ChatClient connectToServer() {
        String serverIpAddress = PropertiesManager.getInstance().getProperty("ipAddress");
        int serverPort = Integer.parseInt(PropertiesManager.getInstance().getProperty("port"));

        try {
            client = new ChatClient(serverIpAddress, serverPort);
        } catch (IOException e) {
            ConnectionAlert.show(String.format("Server: %s:%d is not available...", serverIpAddress, serverPort));
            client = connectToServer();
        }

        return client;
    }

    public ChatClient getClient() {
        return client;
    }
}
