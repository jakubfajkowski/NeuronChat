package client.controller;

import client.alert.ErrorAlert;
import client.network.ChatClient;
import client.network.ChatClientSingleton;
import common.util.Log;
import common.util.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import common.network.ChatMessage;
import common.network.ClientMessage;
import client.network.ChatClientListener;
import common.util.PropertiesManager;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController extends Controller implements ChatClientListener {
    private ChatClient client = ChatClientSingleton.getInstance().getClient();
    private User currentAddressee;

    @FXML private ListView onlineUsersListView;
    @FXML private Tab conversationTab;
    @FXML private TextArea outputTextArea;
    @FXML private TextArea inputTextArea;
    @FXML private Button sendButton;
    @FXML private Tab encryptionTab;
    @FXML private Button negotiateButton;
    @FXML private TextArea logTextField;
    @FXML private TextArea matrixTextField;
    @FXML private TextField serverAddressTextField;
    @FXML private TextField serverPortTextField;
    @FXML private TextField usernameTextField;
    @FXML private Button saveButton;
    @FXML private Button reconnectButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setDefaultProperties();
    }


    private void populatePhoneBook(List<User> users) {
        ObservableList<User> phoneBookRecords = FXCollections.observableArrayList(users);
        Platform.runLater(() -> onlineUsersListView.setItems(phoneBookRecords));
    }

    @Override
    public void handleMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case MESSAGE:
                ChatMessage receivedChatMessage = (ChatMessage)message.getPayload();
                String messageHistory = outputTextArea.getText();
                outputTextArea.setText(messageHistory + receivedChatMessage.toString());
                break;

            case AVAILABLE_USERS:
                List<User> users = (ArrayList<User>) message.getPayload();
                populatePhoneBook(users);
                break;
        }
    }

    private void setDefaultProperties(){
        serverAddressTextField.setText(PropertiesManager.getInstance().getProperty("ipAddress"));
        serverPortTextField.setText(PropertiesManager.getInstance().getProperty("port"));
        usernameTextField.setText(PropertiesManager.getInstance().getProperty("username"));
    }

    public void onlineUsersListView_keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) {
            changeAddressee((User) onlineUsersListView.getSelectionModel().getSelectedItem());
        }
    }

    public void onlineUsersListView_mouseClicked(MouseEvent mouseEvent) {
        changeAddressee((User) onlineUsersListView.getSelectionModel().getSelectedItem());
    }

    private void changeAddressee(User addressee) {
        currentAddressee = addressee;
    }

    public void sendButton_clicked(ActionEvent actionEvent) {
        client.sendMessage(inputTextArea.getText(), currentAddressee);
    }

    public void negotiateButton_clicked(ActionEvent actionEvent) {
    }

    public void usernameTextField_mouseClicked(MouseEvent mouseEvent) {
        usernameTextField.clear();
    }

    public void serverPortTextField_mouseClicked(MouseEvent mouseEvent) {
        serverPortTextField.clear();
    }

    public void serverAddressTextField_mouseClicked(MouseEvent mouseEvent) {
        serverAddressTextField.clear();
    }

    public void saveButton_clicked(ActionEvent actionEvent) {
        try {
            Runtime.getRuntime().exec("java -jar myApp.jar");
            PropertiesManager.getInstance().setProperty("ipAddress", serverAddressTextField.getText());
            PropertiesManager.getInstance().setProperty("port", serverPortTextField.getText());
            PropertiesManager.getInstance().setProperty("username", usernameTextField.getText());

            System.exit(0);
        } catch (IOException e) {
            ErrorAlert.show("Unable to restart application: " + e.getMessage());
        }
    }

    public void reconnectButton_clicked(ActionEvent actionEvent) {
        try {
            client.stop();
            client = ChatClientSingleton.getInstance().connectToServer();

        } catch (IOException e) {
            ErrorAlert.show("Client exception: " + e.getMessage());
        }
    }
}
