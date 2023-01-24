package server;

import database.DataHandler;
import database.FileDataHandler;
import model.UserStatus;
import model.User;
import model.Response;
import model.Request;
import model.RequestType;

import model.chat.Message;
import model.chat.ReActionType;
import model.chat.direct.DirectMessage;
import model.chat.server.Channel;
import model.chat.server.ChannelType;
import model.chat.server.DiscordServer;
import model.chat.server.Role;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {

    public static HashSet<User> users;
    public static ArrayList<ClientHandler> onlineUsers = new ArrayList<>();
    public static DataHandler dataHandler;

    static {
        ClientHandler.dataHandler = new FileDataHandler();
        ClientHandler.users = dataHandler.loadUsers();
    }

    private final Socket clientSocket;
    private String session;
    private int userID;
    private final ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;
    private ObjectOutputStream chatOutputStream;
    private DataOutputStream fileDataOutputStream;
    private DataInputStream fileDataInputStream;

    public ClientHandler(Socket clientSocket) throws IOException, ClassNotFoundException {
        this.clientSocket = clientSocket;
        this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
        this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        this.session = null;
    }

    public String getSession() {
        return session;
    }

    public int getUserID() {
        return userID;
    }

    public ObjectOutputStream getChatOutputStream() {
        return chatOutputStream;
    }

    public DataOutputStream getFileDataOutputStream() {
        return fileDataOutputStream;
    }

    public DataInputStream getFileDataInputStream() {
        return fileDataInputStream;
    }

    public static ClientHandler getClientHandler(int userID) {
        for (ClientHandler clientHandler : onlineUsers) {
            if (clientHandler.getUserID() == userID) {
                return clientHandler;
            }
        }
        return null;
    }

    public static void updateUsers() {
        ClientHandler.users = dataHandler.loadUsers();
    }

    public static ClientHandler getClientHandler(String session) {
        for (ClientHandler clientHandler : onlineUsers) {
            if (clientHandler.getSession().equals(session)) {
                return clientHandler;
            }
        }
        return null;
    }

    public static boolean isUserExist(String userName) {
        for (User user : users) {
            if (user.getUserName().equalsIgnoreCase(userName)) {
                return true;
            }
        }
        return false;
    }

    public static void addUser(User user) {
        users.add(user);
        dataHandler.saveUser(user);
    }

    public void setChatSocket(ObjectOutputStream chatOutputStream) {
        this.chatOutputStream = chatOutputStream;
    }

    public void setFileSocket(DataInputStream fileDataInputStream, DataOutputStream fileDataOutputStream) {
        this.fileDataInputStream = fileDataInputStream;
        this.fileDataOutputStream = fileDataOutputStream;
    }

    @Override
    public void run() {
        setSession();
        while (clientSocket.isConnected()) {
            try {
                Request request = (Request) inputStream.readObject();
                Response response = getResponse(request, session);
                outputStream.writeObject(response);
            } catch (IOException | ClassNotFoundException e) {
                close();
                onlineUsers.remove(this);
                System.out.println("a client has disconnected!");
                try {
                    User user = getUser(session);
                    if (Objects.requireNonNull(user).getStatus().equals(UserStatus.ONLINE)) {
                        Objects.requireNonNull(user).setStatus(UserStatus.OFFLINE);
                    }
                    dataHandler.saveUser(user);
                    ClientHandler.updateUsers();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
    }

    public static User getUser(int ID) {
        for (User user : users) {
            if (user.getID() == ID) {
                return user;
            }
        }
        return null;
    }

    public static User getUser(String session) throws ClassNotFoundException {
        Integer ID = dataHandler.getUserID(session);
        for (User user : users) {
            if (ID != null) {
                if (user.getID() == ID) {
                    return user;
                }
            }
        }
        return null;
    }

    public static User getUserByUsername(String username) {
        for (User user : users) {
            if (user.getUserName().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public static boolean isUserOnline(int userID) {
        for (ClientHandler clientHandler : onlineUsers) {
            try {
                if (dataHandler.getUserID(clientHandler.getSession()) == userID) {
                    return true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void setSession() {
        while (session == null) {
            try {
                Request request = (Request) inputStream.readObject();
                Response response = getResponse(request, session);
                outputStream.writeObject(response);
                if (request.type() == RequestType.AUTHENTICATION) {
                    session = request.request();
                    User user = getUser(session);
                    if (user == null) {
                        session = null;
                        continue;
                    }
                    this.userID = user.getID();
                    onlineUsers.add(this);
                } else if (request.type() == RequestType.SIGN_IN) {
                    if (response.isSuccessful()) {
                        session = (String) response.object();
                        User user = getUser(session);
                        assert user != null;
                        this.userID = user.getID();
                        onlineUsers.add(this);
                    } else {
                        session = null;
                    }
                } else if (request.type() == RequestType.SIGN_UP) {
                    session = null;
                }
            } catch (IOException | ClassNotFoundException ignored) {
            }
        }
    }

    public static boolean isMemberOfChannel(int channelID, int userID) {
        return Objects.requireNonNull(DiscordServerHandler.getDiscordServer(Objects.requireNonNull(ChannelHandler.getChannel(channelID)).getServerID())).getChannels().get(channelID).contains(userID) || Objects.requireNonNull(DiscordServerHandler.getDiscordServer(Objects.requireNonNull(ChannelHandler.getChannel(channelID)).getServerID())).isOwner(userID);
    }

    public void close() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Response getResponse(Request request, String session) throws ClassNotFoundException {
        Response response = null;
        if (request.type() == RequestType.CHANGE_PASSWORD) {
            response = changePassword(request, session);
        } else if (request.type() == RequestType.ADD_FRIEND) {
            response = addFriendRequest(request, session);
        } else if (request.type() == RequestType.SEND_FRIEND_REQUEST) {
            response = sendFriendRequest(request, session);
        } else if (request.type() == RequestType.ACCEPT_FRIEND_REQUEST) {
            response = acceptFriendRequest(request, session);
        } else if (request.type() == RequestType.REJECT_FRIEND_REQUEST) {
            response = rejectFriendRequest(request, session);
        } else if (request.type() == RequestType.BLOCK_USER) {
            response = blockUser(request, session);
        } else if (request.type() == RequestType.UNBLOCK_USER) {
            response = unBlockUser(request, session);
        } else if (request.type() == RequestType.SIGN_IN) {
            response = signIn(request);
        } else if (request.type() == RequestType.SIGN_UP) {
            response = signUp(request);
        } else if (request.type() == RequestType.AUTHENTICATION) {
            response = authentication(request);
        } else if (request.type() == RequestType.MY_ACCOUNT) {
            response = myAccount(session);
        } else if (request.type() == RequestType.CHANGE_PROFILE) {
            response = changeProfile(session);
        } else if (request.type() == RequestType.CHANGE_USERNAME) {
            response = changeUsername(request, session);
        } else if (request.type() == RequestType.CHANGE_EMAIL) {
            response = changeEmail(request, session);
        } else if (request.type() == RequestType.CHANGE_NUMBER_PHONE) {
            response = changeNumberPhone(request, session);
        } else if (request.type() == RequestType.SHOW_LIST_OF_SERVERS) {
            response = showListOfServers(session);
        } else if (request.type() == RequestType.ADD_SERVER) {
            response = addServer(request, session);
        } else if (request.type() == RequestType.ONLINE_FRIENDS) {
            response = onlineFriends(session);
        } else if (request.type() == RequestType.ALL_FRIENDS) {
            response = allFriends(session);
        } else if (request.type() == RequestType.BLOCKED) {
            response = blocked(session);
        } else if (request.type() == RequestType.SHOW_DIRECT_MESSAGES) {
            response = showDirectMessages(session);
        } else if (request.type() == RequestType.START_DIRECT_MESSAGE) {
            response = startDirectMessage(request, session);
        } else if (request.type() == RequestType.LOG_OUT) {
            response = logOut(session);
        } else if (request.type() == RequestType.REMOVE_FRIEND) {
            response = removeFriend(request, session);
        } else if (request.type() == RequestType.CANCEL_FRIEND_REQUEST) {
            response = cancelFriendRequest(request, session);
        } else if (request.type() == RequestType.SHOW_OUTGOING_FRIEND_REQUEST) {
            response = showOutgoingFriendRequest(session);
        } else if (request.type() == RequestType.SHOW_INCOMING_FRIEND_REQUEST) {
            response = showIncomingFriendRequest(session);
        } else if (request.type() == RequestType.SEND_MESSAGE) {
            response = sendMessage(request, session);
        } else if (request.type() == RequestType.CHANGE_NAME) {
            response = changeName(request, session);
        } else if (request.type() == RequestType.SET_STATUS) {
            response = setStatus(request, session);
        } else if (request.type() == RequestType.SHOW_USER) {
            response = showUser(request, session);
        } else if (request.type() == RequestType.START_MESSAGING) {
            response = startMessaging(request, session);
        } else if (request.type() == RequestType.SHOW_CHANNEL) {
            response = showChannel(request, session);
        } else if (request.type() == RequestType.SHOW_USER_IN_SERVER) {
            response = showUserInServer(request, session);
        } else if (request.type() == RequestType.SHOW_SERVER) {
            response = showServer(request, session);
        } else if (request.type() == RequestType.LIST_OF_CHANNELS) {
            response = showListOfChannels();
        } else if (request.type() == RequestType.LIST_OF_TEXT_CHANNELS) {
            response = showListOfTextChannels(request, session);
        } else if (request.type() == RequestType.LIST_OF_VOICE_CHANNELS) {
            response = showListOfVoiceChannel(request, session);
        } else if (request.type() == RequestType.LIST_OF_USERS) {
            response = showListOfUsers(request);
        } else if (request.type() == RequestType.ADD_CHANNEL) {
            response = addChannel(request);
        } else if (request.type() == RequestType.REMOVE_CHANNEL) {
            response = removeChannel(request);
        } else if (request.type() == RequestType.ADD_MEMBER) {
            response = addMember(request, session);
        } else if (request.type() == RequestType.REMOVE_MEMBER) {
            response = removeMember(request);
        } else if (request.type() == RequestType.SEND_FILE) {
            response = sendFile(request, session);
        } else if (request.type() == RequestType.RECEIVE_FILE) {
            response = receiveFile(request, session);
        } else if (request.type() == RequestType.JOIN_CHAT) {
            response = joinChat(request);
        } else if (request.type() == RequestType.CHANGE_SERVER_NAME) {
            response = serverChangeName(request);
        } else if (request.type() == RequestType.ROLES_SETTING) {
            response = roleSetting();
        } else if (request.type() == RequestType.SHOW_ROLE) {
            response = showRole();
        } else if (request.type() == RequestType.CREATE_ROLE) {
            response = createRole(request);
        } else if (request.type() == RequestType.LIST_OF_ROLES) {
            response = showListOfRoles(request);
        } else if (request.type() == RequestType.SHOW_PERMISSIONS) {
            response = showPermissions(request);
        } else if (request.type() == RequestType.EDIT_PERMISSIONS) {
            response = editPermissions(request);
        } else if (request.type() == RequestType.CHANGE_ROLE_NAME) {
            response = changeRoleName(request);
        } else if (request.type() == RequestType.ADD_ROLE_TO_MEMBER) {
            response = showListOfRolesToAdd(request);
        } else if (request.type() == RequestType.ADD_ROLE) {
            response = addRole(request);
        } else if (request.type() == RequestType.REMOVE_ROLE_FROM_MEMBER) {
            response = removeRoleFromMember(request);
        } else if (request.type() == RequestType.REMOVE_MEMBER_ROLE) {
            response = removeMemberRole(request);
        } else if (request.type() == RequestType.PIN_ONE_MESSAGE) {
            response = pinOneMessage(request);
        } else if (request.type() == RequestType.PIN) {
            response = pinFromList(request);
        } else if (request.type() == RequestType.HISTORY_OF_CHAT) {
            response = historyOfChat(request);
        } else if (request.type() == RequestType.LIMIT_MEMBERS_IN_CHANNEL) {
            response = limitMembersOfAChannel(request, session);
        } else if (request.type() == RequestType.SET_MEMBERS_OF_CHANNEL) {
            response = setMembersOfChannel(request, session);
        } else if (request.type() == RequestType.SHOW_MEMBERS_OF_CHANNEL) {
            response = showMembersOfChannel(request);
        } else if (request.type() == RequestType.SHOW_USER_IN_CHANNEL) {
            response = showUserInChannel(request, session);
        } else if (request.type() == RequestType.SHOW_PINNED_MESSAGE) {
            response = showPinnedMessage(request);
        } else if (request.type() == RequestType.LIST_OF_MESSAGES) {
            response = showListOfMessages(request);
        } else if (request.type() == RequestType.PIN_IN_CHAT) {
            response = pinInChat(request, session);
        } else if (request.type() == RequestType.BAN_MEMBER) {
            response = banMember(request);
        } else if (request.type() == RequestType.LIMIT_CHANNELS_OF_MEMBER) {
            response = limitChannelsOfMember(request);
        } else if (request.type() == RequestType.LAUGH) {
            response = laugh(request, session);
        } else if (request.type() == RequestType.LIKE) {
            response = like(request, session);
        } else if (request.type() == RequestType.DISLIKE) {
            response = dislike(request, session);
        } else if (request.type() == RequestType.TEMP) {
            response = temp();
        }
        return response;
    }

    private static Response temp() {
        return new Response(true, "", null);
    }

    private static Response dislike(Request request, String session) {
        Scanner in = new Scanner(request.request());
        String chatID = in.nextLine();
        int senderUser = -1;
        try {
            senderUser = dataHandler.getUserID(session);
        } catch (ClassNotFoundException ignored) {
        }
        int indexOfMessage = Integer.parseInt(in.nextLine());
        int ID = Integer.parseInt(chatID.substring(1));
        if (chatID.contains("d")) {
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(ID);
            assert directMessage != null;
            directMessage.getMessages().get(indexOfMessage).getReActions().add(ReActionType.DISLIKE);
            broadcastMessage(chatID, new Message(senderUser, Objects.requireNonNull(ClientHandler.getUser(senderUser)).getUserName() + " disliked " + Objects.requireNonNull(ClientHandler.getUser(directMessage.getMessages().get(indexOfMessage).getSenderUser())).getUserName(), null), session);
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
        } else if (chatID.contains("c")) {
            Channel channel = ChannelHandler.getChannel(ID);
            assert channel != null;
            channel.getMessages().get(indexOfMessage).getReActions().add(ReActionType.DISLIKE);
            broadcastMessage(chatID, new Message(senderUser, Objects.requireNonNull(ClientHandler.getUser(senderUser)).getUserName() + " disliked " + Objects.requireNonNull(ClientHandler.getUser(channel.getMessages().get(indexOfMessage).getSenderUser())).getUserName(), null), session);
            dataHandler.saveChannel(channel);
            ChannelHandler.updateChannels();
        }
        return new Response(true, "", null);
    }

    private static Response like(Request request, String session) {
        Scanner in = new Scanner(request.request());
        String chatID = in.nextLine();
        int senderUser = -1;
        try {
            senderUser = dataHandler.getUserID(session);
        } catch (ClassNotFoundException ignored) {
        }
        int indexOfMessage = Integer.parseInt(in.nextLine());
        int ID = Integer.parseInt(chatID.substring(1));
        if (chatID.contains("d")) {
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(ID);
            assert directMessage != null;
            directMessage.getMessages().get(indexOfMessage).getReActions().add(ReActionType.LIKE);
            broadcastMessage(chatID, new Message(senderUser, Objects.requireNonNull(ClientHandler.getUser(senderUser)).getUserName() + " liked " + Objects.requireNonNull(ClientHandler.getUser(directMessage.getMessages().get(indexOfMessage).getSenderUser())).getUserName(), null), session);
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
        } else if (chatID.contains("c")) {
            Channel channel = ChannelHandler.getChannel(ID);
            assert channel != null;
            channel.getMessages().get(indexOfMessage).getReActions().add(ReActionType.LIKE);
            broadcastMessage(chatID, new Message(senderUser, Objects.requireNonNull(ClientHandler.getUser(senderUser)).getUserName() + " liked " + Objects.requireNonNull(ClientHandler.getUser(channel.getMessages().get(indexOfMessage).getSenderUser())).getUserName(), null), session);
            dataHandler.saveChannel(channel);
            ChannelHandler.updateChannels();
        }
        return new Response(true, "", null);
    }

    private static Response laugh(Request request, String session) {
        Scanner in = new Scanner(request.request());
        String chatID = in.nextLine();
        int senderUser = -1;
        try {
            senderUser = dataHandler.getUserID(session);
        } catch (ClassNotFoundException ignored) {
        }
        int indexOfMessage = Integer.parseInt(in.nextLine());
        int ID = Integer.parseInt(chatID.substring(1));
        if (chatID.contains("d")) {
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(ID);
            directMessage.getMessages().get(indexOfMessage).getReActions().add(ReActionType.LAUGH);
            broadcastMessage(chatID, new Message(senderUser, ClientHandler.getUser(senderUser).getUserName() + " laughed to " + ClientHandler.getUser(directMessage.getMessages().get(indexOfMessage).getSenderUser()).getUserName(), null), session);
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
        } else if (chatID.contains("c")) {
            Channel channel = ChannelHandler.getChannel(ID);
            channel.getMessages().get(indexOfMessage).getReActions().add(ReActionType.LAUGH);
            broadcastMessage(chatID, new Message(senderUser, ClientHandler.getUser(senderUser).getUserName() + " laughed to " + ClientHandler.getUser(channel.getMessages().get(indexOfMessage).getSenderUser()).getUserName(), null), session);
            dataHandler.saveChannel(channel);
            ChannelHandler.updateChannels();
        }
        return new Response(true, "", null);
    }

    private static Response limitChannelsOfMember(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = -1;
        while (in.hasNextLine()) {
            userID = Integer.parseInt(in.nextLine());
        }
        ArrayList<Integer> objectReceived = (ArrayList<Integer>) request.object();
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        for (Integer integer : objectReceived) {
            discordServer.getChannels().get(integer).remove((Integer) userID);
        }
        dataHandler.saveDiscordServer(discordServer);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, ClientHandler.getUser(userID).getUserName() + " has been banned successfully.", null);
    }

    private static Response banMember(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = -1;
        while (in.hasNextLine()) {
            userID = Integer.parseInt(in.nextLine());
        }
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        HashMap<Integer, HashSet<Integer>> channels = discordServer.getChannels();
        ArrayList<String> toSend = new ArrayList<>();
        for (Integer integer : channels.keySet()) {
            if (channels.get(integer).contains(userID)) {
                toSend.add(integer + "\n" + ChannelHandler.getChannel(integer).getName());
            }
        }
        return new Response(true, "list of the channels that " + ClientHandler.getUser(userID).getUserName() + " is joined.", toSend);
    }

    private static Response pinInChat(Request request, String session) {
        Scanner in = new Scanner(request.request());
        String chatID = in.nextLine();
        int indexOfMessage = Integer.parseInt(in.nextLine());
        int ID = Integer.parseInt(chatID.substring(1));
        User user = null;
        try {
            user = ClientHandler.getUser(session);
        } catch (ClassNotFoundException ignored) {
        }
        if (chatID.contains("d")) {
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(ID);
            directMessage.setPinnedMessage(directMessage.getMessages().get(indexOfMessage));
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
        } else if (chatID.contains("c")) {
            Channel channel = ChannelHandler.getChannel(ID);
            DiscordServer discordServer = DiscordServerHandler.getDiscordServer(channel.getServerID());
            int counter = 0;
            for (Integer integer : discordServer.getMembers().get(user.getID())) {
                Role role = RoleHandler.getRole(integer);
                if (role.getShowChannelPermissions().contains(RequestType.PIN_ONE_MESSAGE)) {
                    counter++;
                }
            }
            if (counter == 0) {
                return new Response(false, "you cannot change pinned message.", null);
            }
            channel.setPinnedMessage(channel.getMessages().get(indexOfMessage));
            dataHandler.saveChannel(channel);
            ChannelHandler.updateChannels();
        } else {
            return new Response(false, "something is wrong.", null);
        }
        return new Response(true, "pinned message has been successfully changed.", null);
    }


    private static Response showListOfMessages(Request request) {
        String chatID = request.request();
        int ID = Integer.parseInt(chatID.substring(1));
        ArrayList<Message> messages = new ArrayList<>();
        if (chatID.contains("c")) {
            messages = ChannelHandler.getChannel(ID).getMessages();
        } else if (chatID.contains("d")) {
            messages = DirectMessageHandler.getDirectMessage(ID).getMessages();
        }
        ArrayList<String> toSend = new ArrayList<>();

        if (messages.size() > 0) {
            for (Message it : messages) {
                toSend.add(messages.indexOf(it) + "\n" + it.toString());
            }
        }
        return new Response(true, "choose one of these messages.", toSend);
    }

    private static Response showPinnedMessage(Request request) {
        String chatID = request.request();
        int ID = Integer.parseInt(chatID.substring(1));
        if (chatID.contains("d")) {
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(ID);
            Message message = directMessage.getPinnedMessage();
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
            return new Response(true, "PINNED MESSAGE : " + message.toString(), null);
        } else if (chatID.contains("c")) {
            Channel channel = ChannelHandler.getChannel(ID);
            Message message = channel.getPinnedMessage();
            return new Response(true, "PINNED MESSAGE : " + message.toString(), null);
        } else {
            return new Response(false, "something is wrong.", null);
        }
    }

    private static Response showUserInChannel(Request request, String session) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt(in.nextLine());
        int userID = Integer.parseInt(in.nextLine());
        User user1 = null;
        try {
            user1 = ClientHandler.getUser(session);
        } catch (ClassNotFoundException ignored) {
        }
        User user2 = ClientHandler.getUser(userID);
        if (user1.getID() == user2.getID()) {
            return new Response(true, "you picked yourself.", null);
        }
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        ArrayList<RequestType> requestTypes = new ArrayList<>(setToSendRequestTypes(user1, user2));
        if (discordServer.isOwner(user2.getID())) {
            requestTypes.add(RequestType.BACK);
            requestTypes.add(RequestType.EXIT);
            return new Response(true, "owner showing", requestTypes);
        }
        if (!discordServer.isOwner(user1.getID())) {
            HashSet<Role> userRoles = new HashSet<>();
            if (discordServer.getMembers().get(user1.getID()) != null) {
                for (Integer integer : discordServer.getMembers().get(user1.getID())) {
                    userRoles.add(RoleHandler.getRole(integer));
                }
                HashSet<RequestType> requestTypes1 = new HashSet<>();
                for (Role role : userRoles) {
                    requestTypes1.addAll(role.getShowUserPermission());
                }
                requestTypes.addAll(requestTypes1);
            }
        } else {
            requestTypes.add(RequestType.ADD_ROLE_TO_MEMBER);
            requestTypes.add(RequestType.REMOVE_ROLE_FROM_MEMBER);
            requestTypes.add(RequestType.REMOVE_MEMBER);
            requestTypes.add(RequestType.BAN_MEMBER);
        }
        requestTypes.add(RequestType.BACK);
        requestTypes.add(RequestType.EXIT);
        return new Response(true, "show user in server", requestTypes);
    }

    private static Response showMembersOfChannel(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt(in.nextLine());
        HashSet<Integer> members = DiscordServerHandler.getDiscordServer(serverID).getChannels().get(channelID);
        ArrayList<String> usersToSend = new ArrayList<>();
        String space = " ";
        for (Integer integer : members) {
            String name = ClientHandler.getUser(integer).getName();
            String status = ClientHandler.getUser(integer).getStatus().toString();
            if (DiscordServerHandler.getDiscordServer(serverID).isOwner(integer)) {
                name += "(Owner)";
            }
            usersToSend.add(integer + "\n" + name + space.repeat(30 - name.length()) + status);
        }
        return new Response(true, "members of " + ChannelHandler.getChannel(channelID).getName(), usersToSend);
    }

    private static Response setMembersOfChannel(Request request, String session) {
        int userID = -1;
        try {
            userID = dataHandler.getUserID(session);
        } catch (ClassNotFoundException ignored) {
        }
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        int channelID = Integer.parseInt(in.nextLine());
        HashSet<Integer> members = new HashSet<>((ArrayList<Integer>) request.object());
        members.add(userID);
        members.add(discordServer.getOwner());
        discordServer.setMembersOfChannel(channelID, members);
        dataHandler.saveDiscordServer(discordServer);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "members of channel has been successfully limited.", null);
    }

    private static Response limitMembersOfAChannel(Request request, String session) {
        Scanner in = new Scanner(request.request());
        int userID = -1;
        try {
            userID = dataHandler.getUserID(session);
        } catch (ClassNotFoundException ignored) {
        }
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt(in.nextLine());
        ArrayList<Integer> members = new ArrayList<>(DiscordServerHandler.getDiscordServer(serverID).getChannels().get(channelID));
        members.remove((Integer) DiscordServerHandler.getDiscordServer(serverID).getOwner());
        members.remove((Integer) userID);
        ArrayList<String> toSend = new ArrayList<>();
        for (Integer integer : members) {
            toSend.add(integer + "\n" + ClientHandler.getUser(integer).getName());
        }
        return new Response(true, "list of members of this channel", toSend);
    }

    private static Response historyOfChat(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt(in.nextLine());
        Channel channel = ChannelHandler.getChannel(channelID);
        ArrayList<Message> messages = channel.getMessages();
        StringBuilder toSend = new StringBuilder();
        if (messages != null) {
            for (Message it : messages) {
                toSend.append(it.toString()).append("\n");
            }
            return new Response(true, toSend.toString(), null);
        } else {
            return new Response(true, "there is not any message to show.", null);
        }
    }

    private static Response pinFromList(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt(in.nextLine());
        int messageIndex = Integer.parseInt(in.nextLine());
        Channel channel = ChannelHandler.getChannel(channelID);
        Message message = channel.getMessages().get(messageIndex);
        channel.setPinnedMessage(message);
        dataHandler.saveChannel(channel);
        ChannelHandler.updateChannels();
        return new Response(true, "the message has been successfully pinned.", null);
    }


    private static Response pinOneMessage(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt((in.nextLine()));
        Channel channel = ChannelHandler.getChannel(channelID);
        ArrayList<Message> messages = channel.getMessages();
        ArrayList<String> toSend = new ArrayList<>();
        if (messages.size() > 0) {
            for (Message it : messages) {
                toSend.add(messages.indexOf(it) + "\n" + it.toString());
            }
        }
        return new Response(true, "choose one of these messages to pin.", toSend);
    }

    private static Response sendFriendRequest(Request request, String session) {
        int userID = Integer.parseInt(request.request());
        User user1 = null;
        try {
            user1 = ClientHandler.getUser(session);
        } catch (ClassNotFoundException ignored) {
        }
        User user2 = ClientHandler.getUser(userID);
        if (user1.getID() != user2.getID()) {
            Objects.requireNonNull(user2).addToFriendRequest(user1.getID());
            user1.getSentFriendRequests().remove((Integer) user2.getID());
            Objects.requireNonNull(user1).getSentFriendRequests().add(user2.getID());
            dataHandler.saveUser(user1);
            dataHandler.saveUser(user2);
            ClientHandler.updateUsers();
            return new Response(true, "your friend request has been successfully sent!", null);
        } else {
            return new Response(false, "you cannot send friend request to yourself", null);
        }
    }

    private static Response removeMemberRole(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = Integer.parseInt(in.nextLine());
        int roleID = Integer.parseInt(in.nextLine());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        assert discordServer != null;
        discordServer.getMembers().get(userID).remove((Integer) roleID);
        dataHandler.saveDiscordServer(discordServer);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "the role has been successfully added to the " + ClientHandler.getUser(userID).getName(), null);
    }

    private static Response removeRoleFromMember(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = Integer.parseInt(in.nextLine());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        assert discordServer != null;
        ArrayList<Integer> roles = discordServer.getMembers().get(userID);
        ArrayList<String> toSend = new ArrayList<>();
        if (roles != null) {
            for (Integer it : roles) {
                toSend.add(it + "\n" + RoleHandler.getRole(it).getRoleName());
            }
        }
        return new Response(true, "list of roles of " + ClientHandler.getUser(userID).getName(), toSend);
    }

    private static Response showListOfRolesToAdd(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = Integer.parseInt(in.nextLine());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        ArrayList<Integer> roles = discordServer.getRoles();
        ArrayList<String> toSend = new ArrayList<>();
        if (discordServer.getMembers().get(userID) != null) {
            for (Integer it : roles) {
                if (!discordServer.getMembers().get(userID).contains(it)) {
                    toSend.add(it + "\n" + RoleHandler.getRole(it).getRoleName());
                }
            }
        } else {
            for (Integer it : roles) {
                toSend.add(it + "\n" + RoleHandler.getRole(it).getRoleName());
            }
        }
        return new Response(true, "list of roles to add to " + ClientHandler.getUser(userID).getName(), toSend);
    }

    private static Response addRole(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = Integer.parseInt(in.nextLine());
        int roleID = Integer.parseInt(in.nextLine());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        if (discordServer.getMembers().get(userID) == null) {
            ArrayList<Integer> roles = new ArrayList<>();
            roles.add(roleID);
            discordServer.getMembers().put(userID, roles);
        } else {
            discordServer.getMembers().get(userID).add(roleID);
        }
        dataHandler.saveDiscordServer(discordServer);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "the role has been successfully added to the " + ClientHandler.getUser(userID).getName(), null);
    }

    private static Response changeRoleName(Request request) {
        Scanner in = new Scanner(request.request());
        int roleID = Integer.parseInt(in.nextLine());
        String newName = in.nextLine();
        Role role = RoleHandler.getRole(roleID);
        role.setRoleName(newName);
        dataHandler.saveRole(role);
        RoleHandler.updateRoles();
        return new Response(true, "name of the role has successfully been changed.", null);
    }

    private static Response editPermissions(Request request) {
        Role role = RoleHandler.getRole(Integer.parseInt(request.request()));
        role.editPermissions((HashMap<RequestType, Boolean>) request.object());
        dataHandler.saveRole(role);
        RoleHandler.updateRoles();
        return new Response(true, "permissions has successfully updated.", null);
    }

    private static Response showPermissions(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int roleID = Integer.parseInt(in.nextLine());
        Role role = RoleHandler.getRole(roleID);
        assert role != null;
        return new Response(true, role.showPermissions(), null);
    }

    private static Response showRole() {
        ArrayList<RequestType> toSend = new ArrayList<>();
        toSend.add(RequestType.SHOW_PERMISSIONS);
        toSend.add(RequestType.EDIT_PERMISSIONS);
        toSend.add(RequestType.CHANGE_ROLE_NAME);
        toSend.add(RequestType.BACK);
        toSend.add(RequestType.EXIT);
        return new Response(true, "show role", toSend);
    }

    private static Response showListOfRoles(Request request) {
        int serverID = Integer.parseInt(request.request());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        ArrayList<Integer> roles = discordServer.getRoles();
        ArrayList<String> toSend = new ArrayList<>();
        for (Integer it : roles) {
            toSend.add(it + "\n" + RoleHandler.getRole(it).getRoleName());
        }
        return new Response(true, "list of roles", toSend);
    }


    private static Response createRole(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        String roleName = in.nextLine();
        HashMap<RequestType, Boolean> permissions = (HashMap<RequestType, Boolean>) request.object();
        Role role = new Role(roleName, permissions);
        dataHandler.saveRole(role);
        RoleHandler.updateRoles();
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        assert discordServer != null;
        discordServer.getRoles().add(role.getID());
        dataHandler.saveDiscordServer(discordServer);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "the role has successfully added.", null);
    }

    private static Response roleSetting() {
        ArrayList<RequestType> toSend = new ArrayList<>();
        toSend.add(RequestType.LIST_OF_ROLES);
        toSend.add(RequestType.CREATE_ROLE);
        toSend.add(RequestType.BACK);
        toSend.add(RequestType.EXIT);
        return new Response(true, "list of channel types.", toSend);
    }

    private static Response serverChangeName(Request request) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        String newName = in.nextLine();
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        assert discordServer != null;
        discordServer.setServerName(newName);
        dataHandler.saveDiscordServer(discordServer);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "name of server has been changed successfully.", null);
    }

    private static Response joinChat(Request request) {
        Scanner in = new Scanner(request.request());
        String serverID = in.nextLine();
        String channelID = in.nextLine();
        Channel channel = ChannelHandler.getChannel(Integer.parseInt(channelID));
        assert channel != null;
        if (channel.getMessages() != null) {
            ArrayList<String> toSend = new ArrayList<>(DirectMessageHandler.getDirectMessageLastMessages(channel.getMessages()));
            return new Response(true, "c" + channelID, toSend);
        } else {
            return new Response(true, "c" + channelID, null);
        }
    }

    private static Response showListOfChannels() {
        ArrayList<RequestType> toSend = new ArrayList<>();
        toSend.add(RequestType.LIST_OF_TEXT_CHANNELS);
        toSend.add(RequestType.LIST_OF_VOICE_CHANNELS);
        toSend.add(RequestType.BACK);
        toSend.add(RequestType.EXIT);
        return new Response(true, "list of channel types.", toSend);
    }

    private static Response showUserInServer(Request request, String session) {
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int userID = Integer.parseInt(in.nextLine());
        User user1 = null;
        try {
            user1 = ClientHandler.getUser(session);
        } catch (ClassNotFoundException ignored) {
        }
        User user2 = ClientHandler.getUser(userID);
        if (user1.getID() == user2.getID()) {
            return new Response(true, "you picked yourself.", null);
        }
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        ArrayList<RequestType> requestTypes = new ArrayList<>(setToSendRequestTypes(user1, user2));
        if (discordServer.isOwner(user2.getID())) {
            requestTypes.add(RequestType.BACK);
            requestTypes.add(RequestType.EXIT);
            return new Response(true, "owner showing", requestTypes);
        }
        if (!discordServer.isOwner(user1.getID())) {
            HashSet<Role> userRoles = new HashSet<>();
            if (discordServer.getMembers().get(user1.getID()) != null) {
                for (Integer integer : discordServer.getMembers().get(user1.getID())) {
                    userRoles.add(RoleHandler.getRole(integer));
                }
                HashSet<RequestType> requestTypes1 = new HashSet<>();
                for (Role role : userRoles) {
                    requestTypes1.addAll(role.getShowUserPermission());
                }
                requestTypes.addAll(requestTypes1);
            }
        } else {
            requestTypes.add(RequestType.ADD_ROLE_TO_MEMBER);
            requestTypes.add(RequestType.REMOVE_ROLE_FROM_MEMBER);
            requestTypes.add(RequestType.REMOVE_MEMBER);
            requestTypes.add(RequestType.BAN_MEMBER);
        }
        requestTypes.add(RequestType.BACK);
        requestTypes.add(RequestType.EXIT);
        return new Response(true, "show user in server", requestTypes);
    }

    private static Response showListOfVoiceChannel(Request request, String session) {
        int serverID = Integer.parseInt(request.request());
        int userID = -1;
        try {
            userID = ClientHandler.getUser(session).getID();
        } catch (ClassNotFoundException ignored) {
        }
        HashSet<Integer> channels = new HashSet<>(DiscordServerHandler.getDiscordServer(serverID).getChannels().keySet());
        ArrayList<String> channelsToSend = new ArrayList<>();
        for (Integer integer : channels) {
            if (ChannelHandler.getChannel(integer).getType() == ChannelType.VOICE_CHANNEL && DiscordServerHandler.getDiscordServer(serverID).getChannels().get(integer).contains(userID)) {
                channelsToSend.add(integer + "\n" + ChannelHandler.getChannel(integer).getName());
            }
        }
        return new Response(true, "voice channels of " + DiscordServerHandler.getDiscordServer(serverID).getServerName(), channelsToSend);
    }

    private static Response showListOfTextChannels(Request request, String session) {
        int serverID = Integer.parseInt(request.request());
        int userID = -1;
        try {
            userID = dataHandler.getUserID(session);
        } catch (ClassNotFoundException ignored) {
        }
        HashSet<Integer> channels = new HashSet<>(DiscordServerHandler.getDiscordServer(serverID).getChannels().keySet());
        ArrayList<String> channelsToSend = new ArrayList<>();
        for (Integer integer : channels) {
            if (ChannelHandler.getChannel(integer).getType() == ChannelType.TEXT_CHANNEL && DiscordServerHandler.getDiscordServer(serverID).getChannels().get(integer).contains((Integer) userID)) {
                channelsToSend.add(integer + "\n" + ChannelHandler.getChannel(integer).getName());
            }
        }
        return new Response(true, "text channels of " + DiscordServerHandler.getDiscordServer(serverID).getServerName(), channelsToSend);
    }

    private static Response showChannel(Request request, String session) {
        User user;
        Scanner in = new Scanner(request.request());
        int serverID = Integer.parseInt(in.nextLine());
        int channelID = Integer.parseInt(in.nextLine());
        Channel channel;
        DiscordServer discordServer;
        ArrayList<RequestType> requestTypes = new ArrayList<>();
        try {
            user = ClientHandler.getUser(session);
            channel = ChannelHandler.getChannel(channelID);
            discordServer = DiscordServerHandler.getDiscordServer(serverID);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something is wrong with server!", null);
        }
        if (channel == null) {
            return new Response(true, null, null);
        }
        requestTypes.add(RequestType.JOIN_CHAT);
        requestTypes.add(RequestType.SHOW_MEMBERS_OF_CHANNEL);
        assert user != null;
        assert discordServer != null;
        if (!discordServer.isOwner(user.getID())) {
            HashSet<Role> userRoles = new HashSet<>();
            if (discordServer.getMembers().get(user.getID()) != null) {
                for (Integer integer : discordServer.getMembers().get(user.getID())) {
                    userRoles.add(RoleHandler.getRole(integer));
                }
                HashSet<RequestType> requestTypes1 = new HashSet<>();
                for (Role role : userRoles) {
                    requestTypes1.addAll(role.getShowChannelPermissions());
                }
                requestTypes.addAll(requestTypes1);
            }
        } else {
            requestTypes.add(RequestType.REMOVE_CHANNEL);
            requestTypes.add(RequestType.HISTORY_OF_CHAT);
            requestTypes.add(RequestType.PIN_ONE_MESSAGE);
            requestTypes.add(RequestType.LIMIT_MEMBERS_IN_CHANNEL);
        }
        if (channel.getType().equals(ChannelType.VOICE_CHANNEL)) {
            requestTypes.remove(RequestType.HISTORY_OF_CHAT);
            requestTypes.remove(RequestType.PIN_ONE_MESSAGE);
            requestTypes.add(0, RequestType.JOIN_GROUP_VOICE_CALL);
            requestTypes.remove(RequestType.JOIN_CHAT);
        }
        requestTypes.add(RequestType.BACK);
        requestTypes.add(RequestType.EXIT);
        return new Response(true, "show channel", requestTypes);
    }

    private static Response showServer(Request request, String session) {
        User user;
        int serverID = Integer.parseInt(request.request());
        DiscordServer discordServer;
        ArrayList<RequestType> requestTypes = new ArrayList<>();
        try {
            user = Objects.requireNonNull(ClientHandler.getUser(session));
            discordServer = DiscordServerHandler.getDiscordServer(serverID);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something is wrong with server!", null);
        }
        requestTypes.add(RequestType.LIST_OF_CHANNELS);
        requestTypes.add(RequestType.LIST_OF_USERS);
        requestTypes.add(RequestType.ADD_MEMBER);
        assert discordServer != null;
        if (discordServer.isOwner(user.getID())) {
            requestTypes.add(RequestType.CHANGE_SERVER_NAME);
            requestTypes.add(RequestType.ADD_CHANNEL);
            requestTypes.add(RequestType.ROLES_SETTING);
        } else {
            HashSet<Role> userRoles = new HashSet<>();
            if (discordServer.getMembers().get(user.getID()) != null) {
                for (Integer integer : discordServer.getMembers().get(user.getID())) {
                    userRoles.add(RoleHandler.getRole(integer));
                }
                HashSet<RequestType> requestTypes1 = new HashSet<>();
                for (Role role : userRoles) {
                    requestTypes1.addAll(role.getShowServerPermissions());
                }
                requestTypes.addAll(requestTypes1);
            }
        }
        requestTypes.add(RequestType.BACK);
        requestTypes.add(RequestType.EXIT);
        return new Response(true, "choose one of choices.", requestTypes);
    }

    private static Response removeMember(Request request) { // get serverID and userID via request
        Scanner scanner = new Scanner(request.request());
        int serverID = Integer.parseInt(scanner.nextLine());
        int userID = -1;
        while (scanner.hasNextLine()) {
            userID = Integer.parseInt(scanner.nextLine());
        }
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        discordServer.getMembers().remove((Integer) userID);
        User user = ClientHandler.getUser(userID);
        user.getDiscordServers().remove((Integer) serverID);
        dataHandler.saveUser(user);
        dataHandler.saveDiscordServer(discordServer);
        ClientHandler.updateUsers();
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "user was successfully removed from the server!", null);
    }

    private static Response addMember(Request request, String session) { // get serverID and username via request
        User user = null;
        try {
            user = ClientHandler.getUser(session);
        } catch (ClassNotFoundException ignored) {
        }
        Scanner scanner = new Scanner(request.request());
        int serverID = Integer.parseInt(scanner.nextLine());
        DiscordServer discordServer = DiscordServerHandler.getDiscordServer(serverID);
        String username = scanner.nextLine();
        if (ClientHandler.isUserExist(username)) {
            User user1 = ClientHandler.getUserByUsername(username);
            assert user1 != null;
            assert user != null;
            if (user.getID() == user1.getID()) {
                return new Response(false, "you add yourself to channel and this is silly.", null);
            }
            if (discordServer.getMembers().containsKey(user1.getID())) {
                return new Response(false, "this user has already joined to server.", null);
            }
            if (user1.isBlocked(user.getID())) {
                return new Response(false, user1.getUserName() + " has blocked you, so you cannot add to server.", null);
            }
            for (Integer it : discordServer.getMembers().keySet()) {
                if (it == user1.getID()) {
                    return new Response(false, "this user has already joined to the server.", null);
                }
            }
            assert discordServer != null;
            discordServer.getMembers().put(user1.getID(), null);
            for (Integer integer : discordServer.getChannels().keySet()) {
                discordServer.getChannels().get(integer).add(user1.getID());
            }
            dataHandler.saveDiscordServer(discordServer);
            DiscordServerHandler.updateDiscordServers();
            user1.getDiscordServers().add(discordServer.getID());
            dataHandler.saveUser(user1);
            ClientHandler.updateUsers();
            return new Response(true, "user was successfully added to the server!", null);
        } else {
            return new Response(false, "this user doesn't exist!", null);
        }
    }

    private static Response removeChannel(Request request) { // get serverID and channelID via request
        Scanner scanner = new Scanner(request.request());
        int serverID = Integer.parseInt(scanner.nextLine());
        int channelID = Integer.parseInt(scanner.nextLine());
        DiscordServerHandler.getDiscordServer(serverID).getChannels().remove((Integer) channelID);
        dataHandler.saveDiscordServer(DiscordServerHandler.getDiscordServer(serverID));
        DiscordServerHandler.updateDiscordServers();
        dataHandler.deleteChannel(channelID);
        ChannelHandler.updateChannels();
        return new Response(true, "channel was successfully deleted!", null);
    }

    private static Response addChannel(Request request) { // get serverID, channelName and channelType via request
        Scanner scanner = new Scanner(request.request());
        int serverID = Integer.parseInt(scanner.nextLine());
        String channelName = scanner.nextLine();
        String channelTypeStr = scanner.nextLine();
        ChannelType channelType;
        if (channelTypeStr.contains("Text")) {
            channelType = ChannelType.TEXT_CHANNEL;
        } else {
            channelType = ChannelType.VOICE_CHANNEL;
        }
        Channel channel = new Channel(channelName, serverID, channelType);
        dataHandler.saveChannel(channel);
        ChannelHandler.updateChannels();
        HashSet<Integer> members = new HashSet<>(DiscordServerHandler.getDiscordServer(serverID).getMembers().keySet());
        members.add(DiscordServerHandler.getDiscordServer(serverID).getOwner());
        DiscordServerHandler.getDiscordServer(serverID).getChannels().put(channel.getID(), members);
        dataHandler.saveDiscordServer(DiscordServerHandler.getDiscordServer(serverID));
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "channel was successfully added!", null);
    }

    private static Response showListOfUsers(Request request) { // get serverID via request
        int serverID = Integer.parseInt(request.request());
        HashMap<Integer, ArrayList<Integer>> users = DiscordServerHandler.getDiscordServer(serverID).getMembers();
        ArrayList<String> usersToSend = new ArrayList<>();
        String space = " ";
        User owner = ClientHandler.getUser(DiscordServerHandler.getDiscordServer(serverID).getOwner());
        assert owner != null;
        String ownerName = owner.getName();
        String ownerStatus = owner.getStatus().toString();
        usersToSend.add(owner.getID() + "\n" + ownerName + "(Owner)" + space.repeat(30 - (ownerName + "(Owner)").length()) + ownerStatus);
        for (Integer integer : users.keySet()) {
            String name = ClientHandler.getUser(integer).getName();
            String status = ClientHandler.getUser(integer).getStatus().toString();
            usersToSend.add(integer + "\n" + name + space.repeat(30 - name.length()) + status);
        }
        return new Response(true, "members of " + DiscordServerHandler.getDiscordServer(serverID).getServerName(), usersToSend);
    }


    private static Response startMessaging(Request request, String session) {
        Scanner in = new Scanner(request.request());
        String userID = "";
        while (in.hasNextLine()) {
            userID = in.nextLine();
        }
        User user1;
        User user2;
        try {
            user1 = ClientHandler.getUser(session);
            user2 = ClientHandler.getUser(Integer.parseInt(userID));
            String chatID;
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(user1.getID(), user2.getID());
            if (directMessage != null) {
                chatID = "d" + directMessage.getID();
                ArrayList<String> toSend = DirectMessageHandler.getDirectMessageLastMessages(directMessage.getMessages());
                return new Response(true, chatID, toSend);
            } else {
                directMessage = new DirectMessage(user1.getID(), user2.getID());
                dataHandler.saveDirectMessage(directMessage);
                DirectMessageHandler.updateDirectMessages();
                user1.getDirectMessages().add(directMessage.getID());
                dataHandler.saveUser(user1);
                user2.getDirectMessages().add(directMessage.getID());
                dataHandler.saveUser(user2);
                ClientHandler.updateUsers();
                chatID = "d" + directMessage.getID();
                return new Response(true, chatID, null);
            }

        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while start messaging!", null);
        }
    }

    private static Response showUser(Request request, String session) {
        String userID = request.request();
        User user1;
        User user2;
        try {
            user1 = ClientHandler.getUser(session);
            user2 = ClientHandler.getUser(Integer.parseInt(userID));
            assert user1 != null;
            assert user2 != null;
            ArrayList<RequestType> requestTypes = new ArrayList<>(setToSendRequestTypes(user1, user2));
            requestTypes.add(RequestType.BACK);
            requestTypes.add(RequestType.EXIT);
            return new Response(true, "showing user", requestTypes);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong while showing user menu", null);
        }
    }

    private static ArrayList<RequestType> setToSendRequestTypes(User user1, User user2) {
        ArrayList<RequestType> requestTypes = new ArrayList<>();
        requestTypes.add(RequestType.START_MESSAGING);
        requestTypes.add(RequestType.JOIN_VOICE_CALL);
        requestTypes.add(RequestType.BLOCK_USER);
        requestTypes.add(RequestType.SEND_FRIEND_REQUEST);
        assert user1 != null;
        assert user2 != null;
        if (user1.isFriend(user2.getID())) {
            requestTypes.add(RequestType.REMOVE_FRIEND);
            requestTypes.remove(RequestType.SEND_FRIEND_REQUEST);
        }
        if (user1.isBlocked(user2.getID())) {
            requestTypes.remove(RequestType.START_MESSAGING);
            requestTypes.remove(RequestType.BLOCK_USER);
            requestTypes.add(RequestType.UNBLOCK_USER);
            requestTypes.remove(RequestType.SEND_FRIEND_REQUEST);
        }
        if (user2.isBlocked(user1.getID())) {
            requestTypes.remove(RequestType.START_MESSAGING);
            requestTypes.remove(RequestType.SEND_FRIEND_REQUEST);
        }
        if (user1.isFriendRequested(user2.getID())) {
            requestTypes.add(RequestType.ACCEPT_FRIEND_REQUEST);
            requestTypes.add(RequestType.REJECT_FRIEND_REQUEST);
            requestTypes.remove(RequestType.SEND_FRIEND_REQUEST);
        }
        if (user1.isFriendRequesting(user2.getID())) {
            requestTypes.add(RequestType.CANCEL_FRIEND_REQUEST);
            requestTypes.remove(RequestType.SEND_FRIEND_REQUEST);
        }
        return requestTypes;
    }

    private static Response showIncomingFriendRequest(String session) {
        try {
            ArrayList<Integer> incomingRequests = Objects.requireNonNull(ClientHandler.getUser(session)).getFriendRequests();
            ArrayList<String> object = new ArrayList<>();
            for (Integer integer : incomingRequests) {
                object.add(integer + "\n" + Objects.requireNonNull(ClientHandler.getUser(integer)).getName());
            }
            return new Response(true, "incoming requests sent!", object);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong while showing incoming requests!", null);
        }
    }

    private static Response showOutgoingFriendRequest(String session) {
        try {
            ArrayList<Integer> outgoingRequests = Objects.requireNonNull(ClientHandler.getUser(session)).getSentFriendRequests();
            ArrayList<String> object = new ArrayList<>();
            for (Integer integer : outgoingRequests) {
                object.add(integer + "\n" + Objects.requireNonNull(ClientHandler.getUser(integer)).getName());
            }
            return new Response(true, "outgoing requests sent!", object);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong while showing outgoing requests!", null);
        }
    }

    private static Response cancelFriendRequest(Request request, String session) { // cancel via userID sent by request
        Scanner in = new Scanner(request.request());
        int userID = -1;
        while (in.hasNextLine()) {
            userID = Integer.parseInt(in.nextLine());
        }
        try {
            User user = ClientHandler.getUser(session);
            Objects.requireNonNull(user).cancelFriendRequest(userID);
            dataHandler.saveUser(user);
            dataHandler.saveUser(ClientHandler.getUser(userID));
            ClientHandler.updateUsers();
            return new Response(true, "friend request has been canceled successfully!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while canceling friend request!", null);
        }
    }

    private static Response removeFriend(Request request, String session) { // remove friend via userID
        Scanner in = new Scanner(request.request());
        int userID = -1;
        while (in.hasNextLine()) {
            userID = Integer.parseInt(in.nextLine());
        }
        try {
            User user = ClientHandler.getUser(session);
            assert user != null;
            int id = user.getID();
            Objects.requireNonNull(user).removeFriend(userID);
            dataHandler.saveUser(user);
            user = ClientHandler.getUser(userID);
            assert user != null;
            user.removeFriend(id);
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
            return new Response(true, "friend removed successfully!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while removing friend!", null);
        }
    }

    private static Response changeName(Request request, String session) {
        try {
            User user = ClientHandler.getUser(session);
            Objects.requireNonNull(user).setName(request.request());
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
            return new Response(true, "your name has been changed successfully!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while changing name!", null);
        }
    }

    private static Response logOut(String session) {
        try {
            try {
                User user = ClientHandler.getUser(session);
                assert user != null;
                if (user.getStatus().equals(UserStatus.ONLINE)) {
                    user.setStatus(UserStatus.OFFLINE);
                    dataHandler.saveUser(user);
                    ClientHandler.updateUsers();
                }
            } catch (ClassNotFoundException ignored) {
            }
            ClientHandler.dataHandler.deleteSession(session);
            return new Response(true, "your logged out successfully!", null);
        } catch (FileNotFoundException e) {
            return new Response(false, "something wrong with the server while logging out!", null);
        }
    }

    private static Response startDirectMessage(Request request, String session) { // send direct message id with 'd' & 25 last message
        User user1 = null;
        User user2 = null;
        try {
            user1 = ClientHandler.getUser(session);
            if (ClientHandler.isUserExist(request.request())) {
                user2 = ClientHandler.getUserByUsername(request.request());
            } else {
                return new Response(false, "user not found.", null);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        DirectMessage directMessage = DirectMessageHandler.getDirectMessage(user1.getID(), user2.getID());
        if (directMessage != null) {
            ArrayList<String> toSend = DirectMessageHandler.getDirectMessageLastMessages(directMessage.getMessages());
            return new Response(true, "d" + directMessage.getID(), toSend);
        } else {
            directMessage = new DirectMessage(user1.getID(), user2.getID());
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
            user1.getDirectMessages().add(directMessage.getID());
            dataHandler.saveUser(user1);
            user2.getDirectMessages().add(directMessage.getID());
            dataHandler.saveUser(user2);
            ClientHandler.updateUsers();
            return new Response(true, "d" + directMessage.getID(), null);
        }
    }

    private static Response showDirectMessages(String session) { // send directID with 'd' and name of directs
        try {
            ArrayList<Integer> directMessages = Objects.requireNonNull(ClientHandler.getUser(session)).getDirectMessages();
            ArrayList<String> directToBeSend = new ArrayList<>();
            String name2;
            int user2ID;
            String space = " ";
            for (Integer integer : directMessages) {
                if (DirectMessageHandler.getDirectMessage(integer).getUser1() != ClientHandler.getUser(session).getID()) {
                    name2 = ClientHandler.getUser(DirectMessageHandler.getDirectMessage(integer).getUser1()).getName();
                    user2ID = DirectMessageHandler.getDirectMessage(integer).getUser1();
                } else {
                    name2 = ClientHandler.getUser(DirectMessageHandler.getDirectMessage(integer).getUser2()).getName();
                    user2ID = DirectMessageHandler.getDirectMessage(integer).getUser2();
                }
                String status = ClientHandler.getUser(user2ID).getStatus().toString();
                directToBeSend.add(user2ID + "\n" + name2 + space.repeat(30 - name2.length()) + status);
            }
            return new Response(true, "direct messages", directToBeSend);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while showing direct messages!", null);
        }
    }

    private static Response blocked(String session) { // send blocked user's name and their userID
        ArrayList<Integer> blockedUsers;
        try {
            blockedUsers = Objects.requireNonNull(ClientHandler.getUser(session)).getBlockedUsers();
            ArrayList<String> blockedUserToBeSend = new ArrayList<>();
            for (Integer integer : blockedUsers) {
                blockedUserToBeSend.add(integer + "\n" + Objects.requireNonNull(ClientHandler.getUser(integer)).getName());
            }
            return new Response(true, "list of blocked users", blockedUserToBeSend);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while show blocked users!", null);
        }
    }

    private static Response allFriends(String session) {
        ArrayList<Integer> friends;
        try {
            friends = Objects.requireNonNull(ClientHandler.getUser(session)).getFriends();
            ArrayList<String> friendsToBeSend = new ArrayList<>();
            String space = " ";
            for (Integer integer : friends) {
                String name = ClientHandler.getUser(integer).getName();
                String status = ClientHandler.getUser(integer).getStatus().toString();
                friendsToBeSend.add(integer + "\n" + name + space.repeat(30 - name.length()) + status);
            }
            return new Response(true, "all friends", friendsToBeSend);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while sending friends!", null);
        }
    }

    private static Response onlineFriends(String session) { // send online friend's names with userID
        try {
            ArrayList<String> online = new ArrayList<>();
            ArrayList<Integer> friends = Objects.requireNonNull(ClientHandler.getUser(session)).getFriends();
            String space = " ";
            for (Integer integer : friends) {
                if (ClientHandler.isUserOnline(integer)) {
                    String name = Objects.requireNonNull(ClientHandler.getUser(integer)).getName();
                    String status = ClientHandler.getUser(integer).getStatus().toString();
                    online.add(integer + "\n" + name + space.repeat(30 - name.length()) + status);
                }
            }
            return new Response(true, "online friends", online);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while show online friends!", null);
        }
    }

    private static Response addServer(Request request, String session) { // get name of server in request
        DiscordServer server;
        try {
            User user = ClientHandler.getUser(session);
            assert user != null;
            server = new DiscordServer(request.request(), user.getID());
            user.getDiscordServers().add(server.getID());
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with server while creating server!", null);
        }
        dataHandler.saveDiscordServer(server);
        DiscordServerHandler.updateDiscordServers();
        return new Response(true, "your server has been successfully created!", null);
    }

    private static Response showListOfServers(String session) throws ClassNotFoundException {
        ArrayList<String> servers = new ArrayList<>();
        for (Integer integer : Objects.requireNonNull(ClientHandler.getUser(session)).getDiscordServers()) {
            servers.add(integer + "\n" + Objects.requireNonNull(DiscordServerHandler.getDiscordServer(integer)).getServerName());
        }
        return new Response(true, "list of servers", servers);
    }

    private static Response changeNumberPhone(Request request, String session) {
        try {
            Scanner in = new Scanner(request.request());
            String currentPassword = in.nextLine();
            String newPhoneNumber = in.nextLine();
            User user = ClientHandler.getUser(session);
            assert user != null;
            if (currentPassword.equals(user.getPassword())) {
                Objects.requireNonNull(user).setPhoneNumber(newPhoneNumber);
                dataHandler.saveUser(user);
                ClientHandler.updateUsers();
            } else {
                return new Response(false, "password is wrong.", null);
            }
            return new Response(true, "your phone number has been changed successfully!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while changing number phone!", null);
        }
    }

    private static Response changeEmail(Request request, String session) {
        try {
            Scanner in = new Scanner(request.request());
            String currentPassword = in.nextLine();
            String newEmail = in.nextLine();
            User user = ClientHandler.getUser(session);
            assert user != null;
            if (currentPassword.equals(user.getPassword())) {
                Objects.requireNonNull(user).setEmail(newEmail);
                dataHandler.saveUser(user);
                ClientHandler.updateUsers();
            } else {
                return new Response(false, "password is wrong.", null);
            }
            return new Response(true, "your email has been changed successfully!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while changing email!", null);
        }
    }

    private static Response changeUsername(Request request, String session) { // get new username via request
        try {
            Scanner in = new Scanner(request.request());
            String currentPassword = in.nextLine();
            String newUsername = in.nextLine();
            User user = ClientHandler.getUser(session);
            assert user != null;
            if (!currentPassword.equals(user.getPassword())) {
                return new Response(false, "password is wrong.", null);
            } else if (ClientHandler.isUserExist(newUsername)) {
                return new Response(false, "this username is already taken!", null);
            } else {
                Objects.requireNonNull(user).setUserName(newUsername);
                dataHandler.saveUser(user);
                ClientHandler.updateUsers();
            }
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with server while changing username!", null);
        }
        return new Response(true, "your username has been changed successfully", null);
    }

    private static Response changeProfile(String session) {
        try {
            User user = ClientHandler.getUser(session);
            ClientHandler client = ClientHandler.getClientHandler(session);
            int bytes;
            String fileName = "";
            try {
                assert client != null;
                fileName = client.getFileDataInputStream().readUTF();
                String imageFormat = fileName.substring(fileName.lastIndexOf("."));
                fileName = user.getID() + imageFormat;
                File file; // where you want to save incoming file path

                if (!new File("src\\database\\file\\profile\\" + user.getID()).exists()) {
                    new File("src\\database\\file\\profile\\" + user.getID()).mkdirs();
                }
                file = new File("src\\database\\file\\profile\\" + user.getID() + "\\" + fileName);

                FileOutputStream fileOutputStream = new FileOutputStream(file);
                long size = client.getFileDataInputStream().readLong();     // read file size
                byte[] buffer = new byte[1024];
                while (size > 0 && (bytes = client.getFileDataInputStream().read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                    fileOutputStream.write(buffer, 0, bytes);
                    size -= bytes;      // read upto file size
                }
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            user.setImage(fileName);
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
            return new Response(true, "your profile has been successfully changed!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while changing profile!", null);
        }
    }

    private static Response myAccount(String session) {
        try {
            String userInfo = Objects.requireNonNull(ClientHandler.getUser(session)).toString();
            return new Response(true, userInfo, Objects.requireNonNull(ClientHandler.getUser(session)).getImage());
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while show your account!", null);
        }
    }

    private static Response authentication(Request request) {
        DataHandler dataHandler = new FileDataHandler();
        try {
            if (dataHandler.getUserID(request.request()) != null) {
                return new Response(true, "you logged in successfully", null);
            } else {
                return new Response(false, "your session not found!\nplease log in", null);
            }
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with server while authentication!", null);
        }
    }

    private static Response signIn(Request request) {
        DataHandler dataHandler = new FileDataHandler();
        Scanner scanner = new Scanner(request.request());
        String userName = scanner.nextLine();
        String password = scanner.nextLine();
        for (User user : ClientHandler.users) {
            if (user.getUserName().equalsIgnoreCase(userName) && user.getPassword().equals(password)) {
                String session = UUID.randomUUID().toString();
                try {
                    dataHandler.saveSession(user.getID(), session);
                } catch (IOException e) {
                    return new Response(false, "something wrong with the server while sign in!", null);
                }
                if (user.getStatus() == null || Objects.requireNonNull(user).getStatus().equals(UserStatus.OFFLINE)) {
                    Objects.requireNonNull(user).setStatus(UserStatus.ONLINE);
                    dataHandler.saveUser(user);
                    ClientHandler.updateUsers();
                }
                return new Response(true, "welcome to your account", session);
            }
        }
        return new Response(false, "user name or password is wrong!", null);
    }

    private static Response signUp(Request request) {
        Scanner scanner = new Scanner(request.request());
        String userName = scanner.nextLine();
        if (ClientHandler.isUserExist(userName)) {
            return new Response(false, "this user name is already taken!\nchoose another one.", null);
        }
        String name = scanner.nextLine();
        String password = scanner.nextLine();
        String email = scanner.nextLine();
        String phoneNumber = null;
        if (scanner.hasNextLine()) {
            phoneNumber = scanner.nextLine();
        }
        User user = new User(userName, name, password, email, phoneNumber, null);
        ClientHandler.addUser(user);
        return new Response(true, "your account has been successfully created!", "");
    }

    private static Response setStatus(Request request, String session) {
        try {
            UserStatus userStatus = (UserStatus) request.object();
            User user = ClientHandler.getUser(session);
            Objects.requireNonNull(user).setStatus(userStatus);
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while setting status!", null);
        }
        return new Response(true, "your status set successfully.", null);
    }

    private static Response unBlockUser(Request request, String session) { //unblock via userID
        Scanner in = new Scanner(request.request());
        String userID = "";
        while (in.hasNextLine()) {
            userID = in.nextLine();
        }
        try {
            User user = ClientHandler.getUser(session);
            Objects.requireNonNull(user).unblockUser(Integer.parseInt(userID));
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
            return new Response(true, "user unblocked successfully!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while unblocking user!", null);
        }
    }

    private static Response blockUser(Request request, String session) { // block user via username
        Scanner in = new Scanner(request.request());
        String userID = "";
        while (in.hasNextLine()) {
            userID = in.nextLine();
        }
        try {
            User user = ClientHandler.getUser(session);
            Objects.requireNonNull(user).blockUser(Integer.parseInt(userID));
            User user2 = ClientHandler.getUser(Integer.parseInt(userID));
            user2.getFriends().remove((Integer) user.getID());
            user2.getFriendRequests().remove((Integer) user.getID());
            user2.getSentFriendRequests().remove((Integer) user.getID());
            dataHandler.saveUser(user2);
            dataHandler.saveUser(user);
            ClientHandler.updateUsers();
            return new Response(true, "user blocked successfully", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while blocking user!", null);
        }
    }

    private static Response rejectFriendRequest(Request request, String session) { // reject friend request via userID
        Scanner in = new Scanner(request.request());
        int userID = -1;
        while (in.hasNextLine()) {
            userID = Integer.parseInt(in.nextLine());
        }
        try {
            User user = ClientHandler.getUser(session);
            Objects.requireNonNull(user).rejectFriendRequest(userID);
            dataHandler.saveUser(user);
            Objects.requireNonNull(ClientHandler.getUser(userID)).getSentFriendRequests().remove((Integer) dataHandler.getUserID(session));
            dataHandler.saveUser(ClientHandler.getUser(userID));
            ClientHandler.updateUsers();
            return new Response(true, "user has been successfully rejected!", null);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while rejecting request!", null);
        }
    }

    private static Response acceptFriendRequest(Request request, String session) { // accept fiend request via userID
        Scanner in = new Scanner(request.request());
        int userID = -1;
        while (in.hasNextLine()) {
            userID = Integer.parseInt(in.nextLine());
        }
        try {
            User user1 = ClientHandler.getUser(session);
            Objects.requireNonNull(user1).acceptFriendRequest(userID);
            dataHandler.saveUser(user1);
            User user2 = ClientHandler.getUser(userID);
            Objects.requireNonNull(user2).getSentFriendRequests().remove((Integer) Objects.requireNonNull(user1).getID());
            user2.getFriendRequests().remove((Integer) user1.getID());
            Objects.requireNonNull(user2).getFriends().add(Objects.requireNonNull(user1).getID());
            dataHandler.saveUser(user2);
            ClientHandler.updateUsers();
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while accepting request!", null);
        }
        return new Response(true, "user has been added to friends!", null);
    }

    private static Response addFriendRequest(Request request, String session) { // send friend request via username
        String userName = request.request();
        User user1;
        try {
            user1 = ClientHandler.getUser(session);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        User user2 = ClientHandler.getUserByUsername(userName);
        if (ClientHandler.isUserExist(userName)) {
            if (user1.getID() != user2.getID()) {
                if (user1.isBlocked(user2.getID())) {
                    return new Response(false, "first unblock " + user2.getUserName() + " then send friend request.", null);
                }
                if (user2.isBlocked(user1.getID())) {
                    return new Response(false, user2.getUserName() + " has blocked you, so you cannot send friend request.", null);
                }
                Objects.requireNonNull(user2).addToFriendRequest(user1.getID());
                user1.getSentFriendRequests().remove((Integer) user2.getID());
                Objects.requireNonNull(user1).getSentFriendRequests().add(user2.getID());
                dataHandler.saveUser(user1);
                dataHandler.saveUser(user2);
                ClientHandler.updateUsers();
                return new Response(true, "your friend request has been successfully sent!", null);
            } else {
                return new Response(false, "you cannot send friend request to yourself", null);
            }
        } else {
            return new Response(false, "user not found!", null);
        }
    }

    private static Response changePassword(Request request, String session) {
        try {
            User user = ClientHandler.getUser(session);
            Scanner in = new Scanner(request.request());
            String currentPassword = in.nextLine();
            String newPassword = in.nextLine();
            assert user != null;
            if (currentPassword.equals(user.getPassword())) {
                Objects.requireNonNull(user).setPassword(newPassword);
                dataHandler.saveUser(user);
                ClientHandler.updateUsers();
            } else {
                return new Response(false, "password is wrong!", null);
            }
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong with the server while changing password!", null);
        }
        return new Response(true, "your password has been changed successfully!", null);
    }

    public static Response sendFile(Request request, String session) { // give chatID with 'd' or 'c'
        // server receives the file
        String chatID = request.request();
        ClientHandler client = ClientHandler.getClientHandler(session);
        int bytes;
        String fileName = "";
        try {
            assert client != null;
            fileName = client.getFileDataInputStream().readUTF();
            File file; // where you want to save incoming file path
            if (chatID.contains("d")) {
                if (!new File("src\\database\\file\\directMessage\\" + chatID.substring(1)).exists()) {
                    new File("src\\database\\file\\directMessage\\" + chatID.substring(1)).mkdirs();
                }
                file = new File("src\\database\\file\\directMessage\\" + chatID.substring(1) + "\\" + fileName);
            } else {
                if (!new File("src\\database\\file\\channel\\" + chatID.substring(1)).exists()) {
                    new File("src\\database\\file\\channel\\" + chatID.substring(1)).mkdirs();
                }
                file = new File("src\\database\\file\\channel\\" + chatID.substring(1) + "\\" + fileName);
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            long size = client.getFileDataInputStream().readLong();     // read file size
            byte[] buffer = new byte[1024];
            while (size > 0 && (bytes = client.getFileDataInputStream().read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                fileOutputStream.write(buffer, 0, bytes);
                size -= bytes;      // read upto file size
            }
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Message message = new Message(ClientHandler.getClientHandler(session).getUserID(), fileName, null);
        broadcastMessage(chatID, message, session);
        return new Response(true, "file has been successfully uploaded!", null);
    }

    private static Response receiveFile(Request request, String session) { // give chatID with 'd' or 'c'
        // server send the file
        String chatID = request.request();
        ClientHandler client = ClientHandler.getClientHandler(session);
        int bytes;
        String fileName;
        try {
            fileName = client.getFileDataInputStream().readUTF();
            File file; // where you want to search for that file
            if (chatID.contains("d")) {
                file = new File("src\\database\\file\\directMessage\\" + chatID.substring(1) + "\\" + fileName);
            } else {
                file = new File("src\\database\\file\\channel\\" + chatID.substring(1) + "\\" + fileName);
            }
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                client.getFileDataOutputStream().writeBoolean(false);
                return new Response(false, "file not founded.", null);
            }
            client.getFileDataOutputStream().writeBoolean(true);
            // send file size
            client.getFileDataOutputStream().writeLong(file.length());
            // break file into chunks
            byte[] buffer = new byte[1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                client.getFileDataOutputStream().write(buffer, 0, bytes);
                client.getFileDataOutputStream().flush();
            }
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Response(true, "file has been successfully downloaded!", null);
    }

    private static Response sendMessage(Request request, String session) { // give chatId with 'd' or 'c' and message
        Scanner scanner = new Scanner(request.request());
        String chatId = scanner.nextLine();
        String message = scanner.nextLine();
        Message messageToSend;
        try {
            messageToSend = new Message(ClientHandler.getUser(session).getID(), message, null);
            broadcastMessage(chatId, messageToSend, session);
        } catch (ClassNotFoundException e) {
            return new Response(false, "something wrong while sending message!", null);
        }
        return new Response(true, "", null); // can be : return null ?.
    }

    private static void broadcastMessage(String chatID, Message message, String session) {
        int id = Integer.parseInt(chatID.substring(1));
        if (chatID.contains("d")) {
            DirectMessage directMessage = DirectMessageHandler.getDirectMessage(id);
            assert directMessage != null;
            directMessage.getMessages().add(message);
            dataHandler.saveDirectMessage(directMessage);
            DirectMessageHandler.updateDirectMessages();
            ArrayList<Object> toSend = new ArrayList<>();
            toSend.add(chatID);
            toSend.add(message.toString());
            try {
                if (ClientHandler.getUser(session).getID() != directMessage.getUser1() && ClientHandler.isUserOnline(directMessage.getUser1())) {
                    toSend.add(ClientHandler.getUser(session).getUserName());
                    toSend.add(ClientHandler.getUser(directMessage.getUser1()).getUserName());
                    toSend.add("Your Direct Message");
                    ClientHandler.getClientHandler(directMessage.getUser1()).getChatOutputStream().writeObject(toSend);
                } else if (ClientHandler.getUser(session).getID() != directMessage.getUser2() && ClientHandler.isUserOnline(directMessage.getUser2())) {
                    toSend.add(ClientHandler.getUser(session).getUserName());
                    toSend.add(ClientHandler.getUser(directMessage.getUser2()).getUserName());
                    toSend.add("Your Direct Message");
                    ClientHandler.getClientHandler(directMessage.getUser2()).getChatOutputStream().writeObject(toSend);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } else if (chatID.contains("c")) {
            int userID = -1;
            try {
                userID = ClientHandler.getUser(session).getID();
            } catch (ClassNotFoundException ignored) {
            }
            Channel channel = ChannelHandler.getChannel(id);
            assert channel != null;
            channel.getMessages().add(message);
            dataHandler.saveChannel(channel);
            ChannelHandler.updateChannels();
            for (ClientHandler clientHandler : ClientHandler.onlineUsers) {
                if (ClientHandler.isMemberOfChannel(id, clientHandler.getUserID()) && clientHandler.getUserID() != userID) {
                    try {
                        ArrayList<Object> toSend = new ArrayList<>();
                        toSend.add(chatID);
                        toSend.add(message.toString());
                        toSend.add(ClientHandler.getUser(session).getUserName());
                        toSend.add(ClientHandler.getUser(clientHandler.getUserID()).getUserName());
                        toSend.add(channel.getName() + " channel");
                        clientHandler.getChatOutputStream().writeObject(toSend);
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}