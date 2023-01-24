package database;

import model.User;
import model.chat.direct.DirectMessage;
import model.chat.server.Channel;
import model.chat.server.DiscordServer;
import model.chat.server.Role;
import server.ChannelHandler;
import server.ClientHandler;
import server.DiscordServerHandler;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class FileDataHandler implements DataHandler {

    @Override
    public Integer getUserID(String session) {
        Scanner scanner = null;
        File path = new File("src\\database\\session");
        File[] files = path.listFiles();
        assert files != null;
        for (File file : files) {
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException ignored) {
            }
            assert scanner != null;
            if (scanner.nextLine().equals(session)) {
                return Integer.parseInt(file.getName().substring(0,file.getName().indexOf(".")));
            }
        }
        return null;
    }

    @Override
    public void saveSession(int userID, String session) {
        FileWriter fileWriter;
        try {
            fileWriter = new FileWriter("src\\database\\session\\" + userID+".txt");
            fileWriter.write(session);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteSession(String session) throws FileNotFoundException {
        Scanner scanner;
        File path = new File("src\\database\\session");
        File[] files = path.listFiles();
        assert files != null;
        for (File file : files) {
            scanner = new Scanner(file);
            if (scanner.nextLine().equals(session)) {
                scanner.close();
                file.delete();
                return;
            }
        }
    }

    @Override
    public HashSet<User> loadUsers() {
        HashSet<User> users = new HashSet<>();
        try {
            FileInputStream fIn;
            ObjectInputStream in;
            File path = new File("src\\database\\user");
            File[] files = path.listFiles();
            assert files != null;
            for (File file : files) {
                fIn = new FileInputStream(file);
                in = new ObjectInputStream(fIn);
                users.add((User) in.readObject());
                fIn.close();
                in.close();
            }
            return users;
        } catch (IOException e) {
            System.out.println("can not open file users");
        } catch (ClassNotFoundException ignored) {
        }
        return users;
    }

    @Override
    public void saveUser(User user) {
        try {
            FileOutputStream fOut = new FileOutputStream("src\\database\\user\\" + user.getID() + ".bin");
            ObjectOutputStream out = new ObjectOutputStream(fOut);
            out.writeObject(user);
            fOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDiscordServer(DiscordServer server) {
        try {
            FileOutputStream fOut = new FileOutputStream("src\\database\\discordServer\\" + server.getID() + ".bin");
            ObjectOutputStream out = new ObjectOutputStream(fOut);
            out.writeObject(server);
            fOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<DiscordServer> loadDiscordServers() {
        HashSet<DiscordServer> servers = new HashSet<>();
        try {
            FileInputStream fIn;
            ObjectInputStream in;
            File path = new File("src\\database\\discordServer");
            File[] files = path.listFiles();
            assert files != null;
            for (File file : files) {
                fIn = new FileInputStream(file);
                in = new ObjectInputStream(fIn);
                servers.add((DiscordServer) in.readObject());
                fIn.close();
                in.close();
            }
            return servers;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("can not open file servers");
        } catch (ClassNotFoundException ignored) {
        }
        return servers;
    }

    @Override
    public void deleteDiscordServer(int serverID) {
        HashMap<Integer,ArrayList<Integer>> members = DiscordServerHandler.getDiscordServer(serverID).getMembers();
        for (Integer integer : members.keySet()) {
            ClientHandler.getUser(integer).getDiscordServers().remove((Integer) serverID);
            saveUser(ClientHandler.getUser(integer));
        }
        ArrayList<Integer> channels = new ArrayList<>(DiscordServerHandler.getDiscordServer(serverID).getChannels().keySet());
        for (Integer integer : channels) {
            deleteChannel(integer);
        }
        DiscordServerHandler.discordServers.remove(DiscordServerHandler.getDiscordServer(serverID));
        File file = new File("src\\database\\discordServer\\" + serverID + ".bin");
        file.delete();
    }

    @Override
    public void saveChannel(Channel channel) {
        try {
            FileOutputStream fOut = new FileOutputStream("src\\database\\channel\\" + channel.getID() + ".bin");
            ObjectOutputStream out = new ObjectOutputStream(fOut);
            out.writeObject(channel);
            fOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<Channel> loadChannels() {
        HashSet<Channel> channels = new HashSet<>();
        try {
            FileInputStream fIn;
            ObjectInputStream in;
            File path = new File("src\\database\\channel");
            File[] files = path.listFiles();
            assert files != null;
            for (File file : files) {
                fIn = new FileInputStream(file);
                in = new ObjectInputStream(fIn);
                channels.add((Channel) in.readObject());
                fIn.close();
                in.close();
            }
            return channels;
        } catch (IOException e) {
            System.out.println("can not open file channel");
        } catch (ClassNotFoundException ignored) {
        }
        return channels;
    }

    @Override
    public void deleteChannel(int channelID) {
        ChannelHandler.channels.remove(ChannelHandler.getChannel(channelID));
        File file = new File("src\\database\\channel\\" + channelID + ".bin");
        file.delete();
    }

    @Override
    public void saveDirectMessage(DirectMessage directMessage) {
        try {
            FileOutputStream fOut = new FileOutputStream("src\\database\\directMessage\\" + directMessage.getID() + ".bin");
            ObjectOutputStream out = new ObjectOutputStream(fOut);
            out.writeObject(directMessage);
            fOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveRole(Role role) {
        try {
            FileOutputStream fOut = new FileOutputStream("src\\database\\role\\" + role.getID() + ".bin");
            ObjectOutputStream out = new ObjectOutputStream(fOut);
            out.writeObject(role);
            fOut.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public HashSet<Role> loadRoles() {
        HashSet<Role> roles = new HashSet<>();
        try {
            FileInputStream fIn;
            ObjectInputStream in;
            File path = new File("src\\database\\role");
            File[] files = path.listFiles();
            assert files != null;
            for (File file : files) {
                fIn = new FileInputStream(file);
                in = new ObjectInputStream(fIn);
                roles.add((Role) in.readObject());
                fIn.close();
                in.close();
            }
            return roles;
        } catch (IOException e) {
            System.out.println("can not open file channel");
        } catch (ClassNotFoundException ignored) {
        }
        return roles;
    }

    @Override
    public HashSet<DirectMessage> loadDirectMessage() {
        HashSet<DirectMessage> directMessages = new HashSet<>();
        try {
            FileInputStream fIn;
            ObjectInputStream in;
            File path = new File("src\\database\\directMessage");
            File[] files = path.listFiles();
            assert files != null;
            for (File file : files) {
                fIn = new FileInputStream(file);
                in = new ObjectInputStream(fIn);
                directMessages.add((DirectMessage) in.readObject());
                fIn.close();
                in.close();
            }
            return directMessages;
        } catch (IOException e) {
            System.out.println("can not open file channel");
        } catch (ClassNotFoundException ignored) {
        }
        return directMessages;
    }

}
