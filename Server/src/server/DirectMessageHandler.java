package server;

import database.DataHandler;
import database.FileDataHandler;
import model.chat.Message;
import model.chat.direct.DirectMessage;

import java.util.ArrayList;
import java.util.HashSet;

public class DirectMessageHandler {

    public static HashSet<DirectMessage> directMessages;
    public static DataHandler dataHandler;

    static {
        DirectMessageHandler.dataHandler = new FileDataHandler();
        DirectMessageHandler.directMessages = dataHandler.loadDirectMessage();
    }

    public static DirectMessage getDirectMessage(int directMessageID) {
        for (DirectMessage directMessage : directMessages) {
            if (directMessage.getID() == directMessageID) {
                return directMessage;
            }
        }
        return null;
    }

    public static DirectMessage getDirectMessage(int userID1, int userID2) {
        for (DirectMessage directMessage : directMessages) {
            if ((directMessage.getUser1() == userID1 && directMessage.getUser2() == userID2) || (directMessage.getUser2() == userID1 && directMessage.getUser1() == userID2)) {
                return directMessage;
            }
        }
        return null;
    }

    public static void updateDirectMessages() {
        DirectMessageHandler.directMessages = dataHandler.loadDirectMessage();
    }

    public static ArrayList<String> getDirectMessageLastMessages(ArrayList<Message> messages) {
        ArrayList<String> toSend = new ArrayList<>();
        if (messages.size() <= 25) {
            if (messages.size() > 0) {
                for (Message message : messages) {
                    toSend.add(message.toString());
                }
            }
        } else {
            for (int i = messages.size() - 1 - 25; i <= messages.size() - 1; i++) {
                toSend.add(messages.get(i).toString());
            }
        }
        return toSend;
    }
}
