package client.alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class InfoAlert{
    public void show(String message){
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("ChatApp");
        alert.setHeaderText("");
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }
}
