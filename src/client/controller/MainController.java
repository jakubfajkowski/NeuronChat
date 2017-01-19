package client.controller;

import client.alert.ErrorAlert;
import client.alert.InfoAlert;
import client.network.ChatClient;
import client.network.ChatClientSingleton;
import client.util.UserListViewItem;
import common.encryption.LearningParameters;
import common.encryption.LearningRule;
import common.encryption.TreeParityMachine;
import common.util.Log;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController extends Controller implements ChatClientListener {
    private ChatClient client;

    private UserListViewItem currentChatPartner;
    private ObservableList<UserListViewItem> onlineUsers;

    @FXML private ListView onlineUsersListView;
    @FXML private Label partnerNameLabel;
    @FXML private TextArea outputTextArea;
    @FXML private TextArea inputTextArea;
    @FXML private ToggleButton negotiateButton;
    @FXML private TextArea keyTextField;
    @FXML private TextArea matrixTextField;
    @FXML private TextField renegotiateAfterTextField;
    @FXML private ChoiceBox learningRuleChoiceBox;
    @FXML private TextField testKeyIntervalTextView;
    @FXML private TextField kValueTextView;
    @FXML private TextField nValueTextView;
    @FXML private TextField lValueTextView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setDefaultProperties();
        initializeLearningRuleChoiceBox();
        client = ChatClientSingleton.getInstance().getClient();
        client.addListener(this);
        initializeOnlineUserListView();
    }

    private void setDefaultProperties() {
        renegotiateAfterTextField.setText(PropertiesManager.getInstance().getProperty("renegotiateAfter"));
        learningRuleChoiceBox.getSelectionModel().select(PropertiesManager.getInstance().getProperty("learningRule"));
        testKeyIntervalTextView.setText(PropertiesManager.getInstance().getProperty("testKeyInterval"));
        kValueTextView.setText(PropertiesManager.getInstance().getProperty("kValue"));
        nValueTextView.setText(PropertiesManager.getInstance().getProperty("nValue"));
        lValueTextView.setText(PropertiesManager.getInstance().getProperty("lValue"));
    }

    private void initializeLearningRuleChoiceBox() {
        learningRuleChoiceBox.getItems().add(LearningRule.HEBBIAN.toString());
        learningRuleChoiceBox.getItems().add(LearningRule.ANTI_HEBBIAN.toString());
        learningRuleChoiceBox.getItems().add(LearningRule.RANDOM_WALK.toString());
    }

    private void initializeOnlineUserListView() {
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

    @Override
    public void handleClientMessage(ClientMessage message) {
        switch (message.getClientMessageMode()) {
            case MESSAGE:
                handleChatMessage(message);
                break;
            case AVAILABLE_USERS:
                onAvailableUsersReceived(message);
                break;
            case TEST_KEY_REQUEST:
                updateEncryptionTab();
                break;
            case FINALIZE_KEY_NEGOTIATION:
                negotiateButton.setSelected(false);
                break;
        }
    }

    private void handleChatMessage(ClientMessage message) {
        ChatMessage receivedChatMessage = (ChatMessage)message.getPayload();

        Optional<UserListViewItem> u = onlineUsers.stream().
                filter(o -> o.getUser().equals(receivedChatMessage.getSender())).
                findFirst();

        if (u.isPresent()) {
            UserListViewItem userListViewItem = u.get();
            userListViewItem.appendChatHistory(message.toString());

            if (currentChatPartner != userListViewItem) {
                enableNotification(userListViewItem);
            }
            else {
                outputTextArea.setText(userListViewItem.getChatHistory());
            }
        }
    }

    private void enableNotification(UserListViewItem userListViewItem) {
        userListViewItem.setUnseenMessage(true);
        onlineUsersListView.refresh();
    }

    private void onAvailableUsersReceived(ClientMessage message) {
        try {
            List<User> users = (ArrayList<User>) message.getPayload();
            users.remove(client.getLocalUser());
            updateOnlineUsersListView(users);
        }
        catch (ClassCastException ignored) {
            Log.print("Unable to read received online user list");
        }
    }

    private void updateOnlineUsersListView(List<User> users) {
        insertNewConnectedUsers(users);
        deleteDisconnectedUsers(users);
        onlineUsers.sort(Comparator.comparing(u -> u.getUser().getUsername()));
    }

    private void insertNewConnectedUsers(List<User> users) {
        for (User u: users) {
            UserListViewItem userListViewItem = new UserListViewItem(u);

            if (!onlineUsers.contains(userListViewItem))
                onlineUsers.add(userListViewItem);
        }
    }

    private void deleteDisconnectedUsers(List<User> users) {
        for (UserListViewItem u: onlineUsers) {
            User user = u.getUser();

            if (!users.contains(user))
                onlineUsers.remove(u);
        }
    }

    private void updateEncryptionTab() {
        TreeParityMachine tpm = client.getServerSession().getTreeParityMachine();
        matrixTextField.setText(tpm.toString());
        keyTextField.setText(DatatypeConverter.printHexBinary(tpm.generateKey()));
    }

    public void onlineUsersListView_keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER) {
            Object selectedItem = onlineUsersListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                UserListViewItem selectedUserListViewItem =
                        (UserListViewItem) selectedItem;

                changeChatPartner(selectedUserListViewItem);
            }
        }
    }

    public void onlineUsersListView_mouseClicked(MouseEvent mouseEvent) {
        Object selectedItem = onlineUsersListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            UserListViewItem selectedUserListViewItem =
                    (UserListViewItem) selectedItem;

            changeChatPartner(selectedUserListViewItem);
        }
    }

    private void changeChatPartner(UserListViewItem chatPartner) {
        currentChatPartner = chatPartner;
        partnerNameLabel.setText(chatPartner.getUser().getUsername());
        outputTextArea.setText(chatPartner.getChatHistory());
        scrollDown();

        disableNotification(chatPartner);
    }

    private void scrollDown() {
        outputTextArea.appendText("");
    }

    private void disableNotification(UserListViewItem userListViewItem) {
        userListViewItem.setUnseenMessage(false);
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
        if (negotiateButton.isSelected()) {
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
        else {
            client.getServerSession().stopSynchronizing();
        }
    }

    public void saveButton_clicked(ActionEvent actionEvent) {
        int k = Integer.valueOf(kValueTextView.getText());
        int n = Integer.valueOf(nValueTextView.getText());

        if (k*n == 16 || k*n == 24 || k*n == 32) {
            PropertiesManager.getInstance().setProperty("renegotiateAfter", renegotiateAfterTextField.getText());
            PropertiesManager.getInstance().setProperty("learningRule",
                    learningRuleChoiceBox.getSelectionModel().getSelectedItem().toString());
            PropertiesManager.getInstance().setProperty("testKeyInterval", testKeyIntervalTextView.getText());
            PropertiesManager.getInstance().setProperty("kValue", kValueTextView.getText());
            PropertiesManager.getInstance().setProperty("nValue", nValueTextView.getText());
            PropertiesManager.getInstance().setProperty("lValue", lValueTextView.getText());
        }
        else {
            ErrorAlert.show(k*n*8 + " bits is not a valid key length.");
            setDefaultProperties();
        }
    }

    public void reconnectButton_clicked(ActionEvent actionEvent) {
        try {
            client.stop();
            ChatClientSingleton.getInstance().initializeNewClientInstance();

        } catch (IOException e) {
            ErrorAlert.show("Client exception: " + e.getMessage());
        }
    }
}
