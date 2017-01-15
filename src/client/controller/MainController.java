package client.controller;

import client.alert.ErrorAlert;
import client.alert.InfoAlert;
import client.network.ChatClient;
import client.network.ChatClientSingleton;
import client.util.UserListViewItem;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController extends Controller implements ChatClientListener {
    private ChatClient client = ChatClientSingleton.getInstance().getClient();

    private UserListViewItem currentChatPartner;
    private ObservableList<UserListViewItem> onlineUsers;

    @FXML private ListView onlineUsersListView;
    @FXML private Label partnerNameLabel;
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
        client.addListener(this);
        onlineUsers = FXCollections.observableArrayList();
        onlineUsersListView.setItems(onlineUsers);

        onlineUsersListView.setCellFactory(cell -> new ListCell<UserListViewItem>() {
            @Override
            protected void updateItem(UserListViewItem item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    //setStyle("-fx-background-color: transparent");
                } else {
                    setText(item.toString());

                    if (item.isUnseenMessage()) {
                        setTextFill(Color.GREEN);
                    } else {
                        setTextFill(Color.BLACK);
                    }
                }
            }
        });


    }

    private void populateOnlineUserListView(List<User> users) {
        for (User u: users) {
            UserListViewItem userListViewItem = new UserListViewItem(u);

            if (!onlineUsers.contains(userListViewItem))
                onlineUsers.add(userListViewItem);
        }

        onlineUsers.sort(Comparator.comparing(u -> u.getUser().getUsername()));
    }

    @Override
    public void handleClientMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case MESSAGE:
                ChatMessage receivedChatMessage = (ChatMessage)message.getPayload();
                handleChatMessage(receivedChatMessage);
                break;

            case AVAILABLE_USERS:
                List<User> users = (ArrayList<User>) message.getPayload();
                users.remove(client.getLocalUser());
                populateOnlineUserListView(users);
                break;
        }
    }

    private void handleChatMessage(ChatMessage message) {
        Optional<UserListViewItem> u = onlineUsers.stream().
                filter(o -> o.getUser().equals(message.getSender())).
                findFirst();

        if (u.isPresent()) {
            UserListViewItem userListViewItem = u.get();
            userListViewItem.appendChatHistory(message.toString());

            if (currentChatPartner != userListViewItem) {
                userListViewItem.setUnseenMessage(true);
                onlineUsersListView.refresh();
            }
            else {
                outputTextArea.setText(userListViewItem.getChatHistory());
            }
        }
    }

    private void setDefaultProperties() {
        serverAddressTextField.setText(PropertiesManager.getInstance().getProperty("ipAddress"));
        serverPortTextField.setText(PropertiesManager.getInstance().getProperty("port"));
        usernameTextField.setText(PropertiesManager.getInstance().getProperty("username"));
    }

    public void onlineUsersListView_keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) {
            UserListViewItem selectedUserListViewItem =
                    (UserListViewItem) onlineUsersListView.getSelectionModel().getSelectedItem();

            changeAddressee(selectedUserListViewItem);
        }
    }

    public void onlineUsersListView_mouseClicked(MouseEvent mouseEvent) {
        UserListViewItem selectedUserListViewItem =
                (UserListViewItem) onlineUsersListView.getSelectionModel().getSelectedItem();

        changeAddressee(selectedUserListViewItem);
    }

    private void changeAddressee(UserListViewItem addressee) {
        User user = addressee.getUser();
        currentChatPartner = addressee;
        partnerNameLabel.setText(user.getUsername());
        outputTextArea.setText(addressee.getChatHistory());
        outputTextArea.appendText("");

        addressee.setUnseenMessage(false);
        onlineUsersListView.refresh();
    }

    public void sendButton_clicked(ActionEvent actionEvent) {
        if (currentChatPartner != null) {
            User localUser = client.getLocalUser();
            ChatMessage messageToSend = new ChatMessage(localUser, inputTextArea.getText());

            client.sendMessage(messageToSend, currentChatPartner.getUser());
            currentChatPartner.appendChatHistory(messageToSend.toString());
            outputTextArea.appendText(messageToSend.toString());
        }
        else{
            InfoAlert.show("You must choose chat partner first!");
        }
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
