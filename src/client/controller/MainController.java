package client.controller;

import client.alert.ErrorAlert;
import client.network.ChatClient;
import common.util.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import common.network.ChatMessage;
import common.network.ClientMessage;
import client.network.ChatClientListener;
import common.util.PropertiesManager;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

public class MainController extends Controller implements ChatClientListener {
    ChatClient client;
    @FXML private ListView phoneBookNames;
    @FXML private Tab conversationTab;
    @FXML private TextArea outputTextField;
    @FXML private TextArea inputTextField;
    @FXML private Button sendButton;
    @FXML private Tab encryptionTab;
    @FXML private Button negotiateButton;
    @FXML private TextArea logTextField;
    @FXML private TextArea matrixTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        PropertiesManager.getInstance().setFileName("options");
        String serverIpAddress = PropertiesManager.getInstance().getProperty("ipAddress");
        int serverPort = Integer.parseInt(PropertiesManager.getInstance().getProperty("port"));

        connectToServer(serverIpAddress, serverPort);
    }

    private void connectToServer(String ipAddress, int port) {
        try {
            client = new ChatClient(ipAddress, port);
            client.addListener(this);
            client.sendAvailableUsersRequest();
        } catch (IOException e) {
            ErrorAlert.show(MessageFormat.format("Server: {0}:{1} is not available...", ipAddress, port));
        }
    }

    private void populatePhoneBook(List<User> users) {
        ObservableList<User> phoneBookRecords = FXCollections.observableArrayList(users);

        phoneBookNames.setItems(phoneBookRecords);
    }

    @Override
    public void handleMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case MESSAGE:
                ChatMessage receivedChatMessage = (ChatMessage)message.getPayload();
                String messageHistory = outputTextField.getText();
                outputTextField.setText(messageHistory + receivedChatMessage.toString());
                break;
            case AVAILABLE_USERS:
                List<User> users = (ArrayList<User>) message.getPayload();
                populatePhoneBook(users);
                break;
            case CONNECTION:
                client.sendAvailableUsersRequest();
                break;
        }
    }
}
