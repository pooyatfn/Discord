package server;

import database.DataHandler;
import database.FileDataHandler;
import model.chat.server.DiscordServer;

import java.util.HashSet;

public class DiscordServerHandler {

    public static HashSet<DiscordServer> discordServers;
    public static DataHandler dataHandler;

    static {
        DiscordServerHandler.dataHandler = new FileDataHandler();
        DiscordServerHandler.discordServers = dataHandler.loadDiscordServers();
    }

    public static DiscordServer getDiscordServer(int serverID) {
        for (DiscordServer discordServer : discordServers) {
            if (discordServer.getID() == serverID) {
                return discordServer;
            }
        }
        return null;
    }

    public static void updateDiscordServers() {
        DiscordServerHandler.discordServers = dataHandler.loadDiscordServers();
    }


}
