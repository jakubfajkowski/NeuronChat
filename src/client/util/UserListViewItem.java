package client.util;

import common.util.User;

public class UserListViewItem {
    private User user;
    private String chatHistory;
    private boolean unseenMessage;

    public UserListViewItem(User user) {
        this.user = user;
        this.chatHistory = "";
        this.unseenMessage = false;
    }

    public User getUser() {
        return user;
    }

    public String getChatHistory() {
        return chatHistory;
    }

    public void appendChatHistory(String message) {
        this.chatHistory = this.chatHistory + message;
    }

    public boolean isUnseenMessage() {
        return unseenMessage;
    }

    public void setUnseenMessage(boolean unseenMessage) {
        this.unseenMessage = unseenMessage;
    }

    @Override
    public String toString() {
        return user.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserListViewItem that = (UserListViewItem) o;

        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }
}
