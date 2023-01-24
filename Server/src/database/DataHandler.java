package database;

import model.User;
import model.chat.direct.DirectMessage;
import model.chat.server.Channel;
import model.chat.server.DiscordServer;
import model.chat.server.Role;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;

public interface DataHandler {

    Integer getUserID(String session) throws ClassNotFoundException;
    void saveSession(int userID, String session) throws IOException;
    void deleteSession(String session) throws FileNotFoundException;
    HashSet<User> loadUsers();
    void saveUser(User user);
    HashSet<DiscordServer> loadDiscordServers();
    void saveDiscordServer(DiscordServer server);
    void deleteDiscordServer(int serverID);
    HashSet<Channel> loadChannels();
    void saveChannel(Channel channel);
    void deleteChannel(int channelID);
    HashSet<DirectMessage> loadDirectMessage();
    void saveDirectMessage(DirectMessage directMessage);
    void saveRole(Role role);
    HashSet<Role> loadRoles();
}
