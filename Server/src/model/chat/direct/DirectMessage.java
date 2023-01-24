package model.chat.direct;

import model.chat.Message;
import server.DirectMessageHandler;

import java.io.Serializable;
import java.util.ArrayList;

public class DirectMessage implements Serializable {

    private final int ID;
    private final int user1;
    private final int user2;
    private final ArrayList<Message> messages = new ArrayList<>();
    private Message pinnedMessage;


    public DirectMessage(int user1, int user2) {
        this.ID = DirectMessageHandler.directMessages.size();
        this.user1 = user1;
        this.user2 = user2;
    }

    public int getID() {
        return ID;
    }

    public int getUser1() {
        return user1;
    }

    public int getUser2() {
        return user2;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public Message getPinnedMessage() {
        return pinnedMessage;
    }

    public void setPinnedMessage(Message pinnedMessage) {
        this.pinnedMessage = pinnedMessage;
    }
}
