package server.network;

import common.util.PropertiesManager;
import common.util.UserCredentials;

public class AuthenticationService {
    private PropertiesManager propertiesManager = PropertiesManager.getInstance();

    public AuthenticationService() {
        propertiesManager.setFileName("credentials");
    }

    public boolean validate(UserCredentials clientCredentials) {
        return isValidPassword(clientCredentials);
    }

    private boolean isValidPassword(UserCredentials clientCredentials) {
        String username = clientCredentials.getUsername();
        String passwordHash = clientCredentials.getPasswordHashString();

        return isUsed(username) && propertiesManager.getProperty(username).equals(passwordHash);
    }

    public boolean register(UserCredentials clientCredentials) {
        return addNewCredentialsToPropertiesFile(clientCredentials);
    }

    private boolean addNewCredentialsToPropertiesFile(UserCredentials clientCredentials) {
        String username = clientCredentials.getUsername();
        String passwordHash = clientCredentials.getPasswordHashString();

        if(!isUsed(username)){
            propertiesManager.setProperty(username, passwordHash);
            return true;
        }else
            return false;
    }

    private boolean isUsed(String login) {
        return propertiesManager.getProperty(login) != null;
    }
}