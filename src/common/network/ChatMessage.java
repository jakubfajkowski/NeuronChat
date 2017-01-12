package common.network;

import java.io.Serializable;
import java.util.Date;

public class ChatMessage implements Serializable {
    private String sender;
    private String text;
    private Date dateCreated;

    public ChatMessage(String sender, String text) {
        this.sender = sender;
        this.text = text;
        dateCreated  = new Date();
    }

    @Override
    public String toString() {
        return sender + " " + dateCreated.toString() + "\n\n"
                + text + "\n";
    }
}
