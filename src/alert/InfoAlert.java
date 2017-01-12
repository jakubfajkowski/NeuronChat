package alert;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class InfoAlert extends Alert{
    public InfoAlert(String message){
        super(AlertType.INFORMATION);
        setTitle("ChatApp");
        setHeaderText("");
        setContentText(message);
        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.initStyle(StageStyle.UTILITY);
        showAndWait();
    }
}
