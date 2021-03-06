package common.util;

import javafx.scene.control.Alert;

import java.io.*;
import java.util.Properties;
/**
 * Klasa odpowiadająca za pobieranie i ustawianie wartości w pliku properties
 */
public class PropertiesManager {
    private String fileName;
    private Properties properties = new Properties();

    private static PropertiesManager propertiesManager = new PropertiesManager();
    public static PropertiesManager getInstance() {
        return propertiesManager;
    }
    private PropertiesManager() {}

    public void setProperty(String key, String value){
        properties.setProperty(key, value);
        save();
    }

    public String getProperty(String key){
        load();
        return properties.getProperty(key);
    }

    private void save(){
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(fileName + ".properties");
            properties.store(fileOutputStream, "Locally generated credentials file");
        } catch (IOException e) {
            showErrorDialog(e);
        }

    }

    private void load(){
        InputStream inputStream;
        try {
            inputStream = new FileInputStream(fileName + ".properties");
        } catch (FileNotFoundException e) {
            inputStream = getClass().getResourceAsStream(fileName + ".properties");
        }

        try {
            properties.load(inputStream);
        } catch (IOException e) {
            showErrorDialog(e);
        }

    }

    private void showErrorDialog(Exception e){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Properties manager error.");
        alert.setContentText(e.getMessage());

        alert.showAndWait();
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}