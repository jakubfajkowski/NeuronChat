package client.controller;

import client.alert.ErrorAlert;
import client.network.ChatClient;
import common.util.User;
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
    private ChatClient client;
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


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertiesManager.getInstance().setFileName("options");
        String serverIpAddress = PropertiesManager.getInstance().getProperty("ipAddress");
        int serverPort = Integer.parseInt(PropertiesManager.getInstance().getProperty("port"));
        String username = PropertiesManager.getInstance().getProperty("username");

        connectToServer(serverIpAddress, serverPort, username);

        setDefaultProperties();
    }

    private void connectToServer(String ipAddress, int port, String username) {
        try {
            client = new ChatClient(ipAddress, port, new User(username));
            client.addListener(this);
        } catch (IOException e) {
            ErrorAlert.show(String.format("Server: %s:%d is not available...", ipAddress, port));
        }
    }

    private void populatePhoneBook(List<User> users) {
        ObservableList<User> phoneBookRecords = FXCollections.observableArrayList(users);

        onlineUsersListView.setItems(phoneBookRecords);
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
        PropertiesManager.getInstance().setProperty("ipAddress", serverAddressTextField.getText());
        PropertiesManager.getInstance().setProperty("port", serverPortTextField.getText());
        PropertiesManager.getInstance().setProperty("username", usernameTextField.getText());
    }
}
