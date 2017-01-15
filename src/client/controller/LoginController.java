package client.controller;

import client.Main;
import client.alert.ErrorAlert;
import client.alert.InfoAlert;
import client.network.ChatClient;
import client.network.ChatClientListener;
import client.network.ChatClientSingleton;
import common.network.ClientMessage;
import common.util.User;
import common.util.UserCredentials;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController extends Controller implements ChatClientListener {
    private ChatClient client = ChatClientSingleton.getInstance().getClient();

    @FXML public TextField usernameTextField;
    @FXML public PasswordField passwordField;
    @FXML public Button loginButton;
    @FXML public Button registerButton;
    @FXML public Button cancelButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        //Login and register buttons are locked when username form is empty.
        loginButton.setDisable(true);
        registerButton.setDisable(true);
        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
            registerButton.setDisable(newValue.trim().isEmpty());
        });

        client.addListener(this);
        usernameTextField.requestFocus();
    }

    @Override
    public void handleMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case LOGIN:
                processLoginResponse((boolean) message.getPayload());
                break;
            case REGISTER:
                processRegisterResponse((boolean) message.getPayload());
                break;
        }
    }

    public void loginButton_onClick() {
        UserCredentials userCredentials = collectUserCredentials();
        client.sendLoginRequest(userCredentials);
    }

    private void processLoginResponse(boolean response) {
        String loginResponseString = getLoginResponseString(response);
        Platform.runLater(() -> InfoAlert.show(loginResponseString));

        if (response){
            User user = new User(usernameTextField.getText());
            client.setLocalUser(user);
            Platform.runLater(() -> prepareMainView(user));
        }
    }

    private String getLoginResponseString(boolean response) {
        return response ? "Logged in." : "Invalid username/password.";
    }

    private void prepareMainView(User user) {
        Stage primaryStage = Main.getPrimaryStage();

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/mainView.fxml"));
            setUsernameInTitle(user, primaryStage);
            primaryStage.setScene(new Scene(root, 600, 400));
            primaryStage.setResizable(false);
        } catch (IOException e) {
            ErrorAlert.show("Unable to locate main window view file.");
        }
    }

    private void setUsernameInTitle(User user, Stage stage) {
        stage.setTitle(Main.APPLICATION_NAME + " (" + user.getUsername() + ")");
    }

    public void registerButton_onClick() {
        UserCredentials userCredentials = collectUserCredentials();
        client.sendRegisterRequest(userCredentials);
    }

    private void processRegisterResponse(boolean response) {
        String registerResponseString = getRegisterResponseString(response);
        Platform.runLater(() -> InfoAlert.show(registerResponseString));
    }

    private String getRegisterResponseString(boolean response) {
        return response ? "Registration successful." : "Username already taken.";
    }

    private UserCredentials collectUserCredentials(){
        String username = usernameTextField.getText(),
               password = passwordField.getText();

        return new UserCredentials(username, password);
    }

    public void cancelButton_onClick(ActionEvent actionEvent) {
        System.exit(0);
    }

    public void usernameTextField_onKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) {
            passwordField.requestFocus();
        }
    }

    public void passwordField_onKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) {
            loginButton.requestFocus();
        }
    }

    public void button_onKeyPressed(KeyEvent keyEvent) {
        Button source = (Button)keyEvent.getSource();

        if(keyEvent.getCode() == KeyCode.ENTER) {
            source.fire();
        }
    }
}
