package client.alert;

import common.util.PropertiesManager;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class ConnectionAlert {
    public static void show(String message) {
        Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle("Connection error");
        dialog.setHeaderText(message);

        dialog.setGraphic(new ImageView(ConnectionAlert.class.getResource("connection.png").toString()));

        ButtonType reconnectButtonType = new ButtonType("Reconnect", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(reconnectButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serverAddressTextField = new TextField();
        serverAddressTextField.setText(PropertiesManager.getInstance().getProperty("ipAddress"));
        TextField serverPortTextField = new TextField();
        serverPortTextField.setText(PropertiesManager.getInstance().getProperty("port"));

        grid.add(new Label("IP Address:"), 0, 0);
        grid.add(serverAddressTextField, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(serverPortTextField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == reconnectButtonType) {
                PropertiesManager.getInstance().setProperty("ipAddress", serverAddressTextField.getText());
                PropertiesManager.getInstance().setProperty("port", serverPortTextField.getText());

                return new Pair<>(serverAddressTextField.getText(), Integer.parseInt(serverPortTextField.getText()));
            }

            System.exit(0);
            return null;
        });

        dialog.showAndWait();
    }
}
