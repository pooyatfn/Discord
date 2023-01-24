package server;

import database.DataHandler;
import database.FileDataHandler;
import model.chat.server.Channel;

import java.util.HashSet;

public class ChannelHandler {

    public static HashSet<Channel> channels;
    public static DataHandler dataHandler;

    static {
        ChannelHandler.dataHandler = new FileDataHandler();
        ChannelHandler.channels = dataHandler.loadChannels();
    }

    public static Channel getChannel(int channelID) {
        for (Channel channel : channels) {
            if (channel.getID() == channelID) {
                return channel;
            }
        }
        return null;
    }

    public static void updateChannels() {
        ChannelHandler.channels = dataHandler.loadChannels();
    }

}
