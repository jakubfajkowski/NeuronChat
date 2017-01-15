package client;

import common.util.PropertiesManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;
    public static final String APPLICATION_NAME = "MiniChatApp";

    @Override
    public void start(Stage primaryStage) throws Exception{
        PropertiesManager.getInstance().setFileName("options");

        Parent root = FXMLLoader.load(getClass().getResource("/client/loginView.fxml"));
        Main.primaryStage = primaryStage;
        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setScene(new Scene(root, 250, 110));
        primaryStage.setResizable(false);
        Main.primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void restart() throws IOException {
        Runtime.getRuntime().exec("java -jar " + APPLICATION_NAME + ".jar");
        System.exit(0);
    }
}
