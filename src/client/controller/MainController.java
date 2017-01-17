package client.controller;

import client.alert.ErrorAlert;
import client.alert.InfoAlert;
import client.network.ChatClient;
import client.network.ChatClientSingleton;
import client.util.UserListViewItem;
import common.encryption.LearningParameters;
import common.encryption.LearningRule;
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
    @FXML private TextField renegotiateAfterTextField;
    @FXML private ChoiceBox learningRuleChoiceBox;
    @FXML private TextField testKeyIntervalTextView;
    @FXML private TextField kValueTextView;
    @FXML private TextField nValueTextView;
    @FXML private TextField lValueTextView;
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
        renegotiateAfterTextField.setText(PropertiesManager.getInstance().getProperty("renegotiateAfter"));
        learningRuleChoiceBox.getSelectionModel().select(PropertiesManager.getInstance().getProperty("learningRule"));
        testKeyIntervalTextView.setText(PropertiesManager.getInstance().getProperty("testKeyInterval"));
        kValueTextView.setText(PropertiesManager.getInstance().getProperty("kValue"));
        nValueTextView.setText(PropertiesManager.getInstance().getProperty("nValue"));
        lValueTextView.setText(PropertiesManager.getInstance().getProperty("lValue"));
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
        LearningParameters lp = new LearningParameters(
                LearningRule.valueOf(learningRuleChoiceBox.getSelectionModel().getSelectedItem().toString()),
                Integer.valueOf(kValueTextView.getText()),
                Integer.valueOf(nValueTextView.getText()),
                Integer.valueOf(lValueTextView.getText()),
                Integer.valueOf(testKeyIntervalTextView.getText()),
                Integer.valueOf(renegotiateAfterTextField.getText())
        );

        client.sendInitializeKeyNegotiationRequest(lp);
    }

    public void saveButton_clicked(ActionEvent actionEvent) {
        PropertiesManager.getInstance().setProperty("ipAddress", serverAddressTextField.getText());
        PropertiesManager.getInstance().setProperty("port", serverPortTextField.getText());
        PropertiesManager.getInstance().setProperty("renegotiateAfter", renegotiateAfterTextField.getText());
        PropertiesManager.getInstance().setProperty("learningRule",
                learningRuleChoiceBox.getSelectionModel().getSelectedItem().toString());
        PropertiesManager.getInstance().setProperty("kValue", kValueTextView.getText());
        PropertiesManager.getInstance().setProperty("nValue", nValueTextView.getText());
        PropertiesManager.getInstance().setProperty("lValue", lValueTextView.getText());
        PropertiesManager.getInstance().setProperty("testKeyInterval", testKeyIntervalTextView.getText());

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
