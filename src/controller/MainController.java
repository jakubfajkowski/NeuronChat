package controller;

import alert.ErrorAlert;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import network.Client;
import util.PhoneBookRecord;
import util.PropertiesManager;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

/**
 * Created by FajQa on 12.01.2017.
 */
public class MainController extends Controller {
    Client client;
    private Dictionary<String, PhoneBookRecord> phoneBookDictionary = new Hashtable<>();

    @FXML public ListView phoneBookNames;
    @FXML public Tab conversationTab;
    @FXML public TextArea outputTextField;
    @FXML public TextArea inputTextField;
    @FXML public Button sendButton;
    @FXML public Tab encryptionTab;
    @FXML public Button negotiateButton;
    @FXML public TextArea logTextField;
    @FXML public TextArea matrixTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        populatePhoneBookRecords();
    }

    private void populatePhoneBookRecords() {
        PropertiesManager.getInstance().setFileName("phoneBook");
        String[] ips = PropertiesManager.getInstance().getProperty("ip").split(";");
        String[] ports = PropertiesManager.getInstance().getProperty("port").split(";");
        String[] names = PropertiesManager.getInstance().getProperty("name").split(";");

        ObservableList<String> phoneBookNames = FXCollections.observableArrayList();
        for (int i = 0; i < names.length; i++) {
            try {
                InetAddress ip = InetAddress.getByName(ips[i]);
                int port = Integer.parseInt(ports[i]);
                String name = names[i];

                phoneBookDictionary.put(name, new PhoneBookRecord(ip, port));
                phoneBookNames.add(name);
            } catch (UnknownHostException e) {
                new ErrorAlert("Unable to resolve phone book record: " + names[i] + ".");
            }
        }

        //noinspection unchecked
        this.phoneBookNames.setItems(phoneBookNames);
    }
}
