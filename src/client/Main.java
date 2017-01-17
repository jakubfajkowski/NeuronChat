package client;

import client.network.ChatClientSingleton;
import common.util.PropertiesManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;
    public static final String APPLICATION_NAME = "Neuron Chat";

    @Override
    public void start(Stage primaryStage) throws Exception{
        PropertiesManager.getInstance().setFileName("options");
        Main.primaryStage = primaryStage;
        ChatClientSingleton.getInstance().initializeNewClientInstance();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
