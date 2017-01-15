package client.alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ErrorAlert {
    public static void show(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("");
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }
}