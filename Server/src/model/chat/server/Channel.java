package model.chat.server;

import model.chat.Message;
import server.ChannelHandler;

import java.io.Serializable;
import java.util.ArrayList;

public class Channel implements Serializable {

    private final int ID;
    private final int serverID;
    private final String name;
    private final ChannelType type;
    private final ArrayList<Message> messages = new ArrayList<>();
    private Message pinnedMessage;

    public Channel(String name, int serverID, ChannelType type) {
        this.ID = ChannelHandler.channels.size();
        this.serverID = serverID;
        this.name = name;
        this.type = type;
        this.pinnedMessage = null;
    }

    public int getServerID() {
        return serverID;
    }

    public int getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public ChannelType getType() {
        return type;
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