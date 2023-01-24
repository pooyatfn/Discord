package model.chat.server;

import server.DiscordServerHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DiscordServer implements Serializable {

    private final int ID;
    private String serverName;
    private final int owner;
    private final HashMap<Integer, HashSet<Integer>> channels = new HashMap<>();
    // key is the member and value is the roles of members in members hash map.
    private final HashMap<Integer, ArrayList<Integer>> members = new HashMap<>();
    private final ArrayList<Integer> roles = new ArrayList<>();

    public DiscordServer(String serverName, int owner) {
        this.ID = DiscordServerHandler.discordServers.size();
        this.serverName = serverName;
        this.owner = owner;
    }

    public void setServerName(String newName) {
        this.serverName = newName;
    }

    public ArrayList<Integer> getRoles() {
        return roles;
    }

    public int getID() {
        return ID;
    }

    public String getServerName() {
        return serverName;
    }

    public Integer getOwner() {
        return owner;
    }

    public HashMap<Integer, HashSet<Integer>> getChannels() {
        return channels;
    }

    public void setMembersOfChannel(int channelID, HashSet<Integer> members) {
        channels.put(channelID, members);
    }

    public HashMap<Integer, ArrayList<Integer>> getMembers() {
        return members;
    }

    public boolean isOwner(int userID) {
        return userID == owner;
    }
}