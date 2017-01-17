package client.network;

import client.Main;
import client.alert.ConnectionAlert;
import common.util.PropertiesManager;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import static client.Main.APPLICATION_NAME;

public class ChatClientSingleton {
    private ChatClient client;

    private static ChatClientSingleton ourInstance = new ChatClientSingleton();

    public static ChatClientSingleton getInstance() {
        return ourInstance;
    }

    private ChatClientSingleton() {}

    public void initializeNewClientInstance() {
        connectToServer();
        showLoginDialog();
    }

    private void showLoginDialog() {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/client/loginView.fxml"));
            Stage primaryStage = Main.getPrimaryStage();
            primaryStage.setTitle(APPLICATION_NAME);
            primaryStage.setScene(new Scene(root, 250, 110));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ChatClient connectToServer() {
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
