package common.network;

import common.util.User;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    private User sender;
    private String text;
    private Date dateCreated;

    public ChatMessage(User sender, String text) {
        this.sender = sender;
        this.text = text;
        dateCreated  = new Date();
    }

    @Override
    public String toString() {
        return sender + " --- " + dateCreated.toString() + "\n"
                + text + "\n\n";
    }
}
