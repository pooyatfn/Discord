package Client;

import Exception.*;
import Menu.Menu;
import Menu.MenuFactory;
import Menu.MenuType;
import model.Request;
import model.RequestType;
import model.Response;
import model.UserStatus;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientRequestSender {
    //to send request
    private static ObjectOutputStream objectOutputStream;
    //to receive response
    private static ObjectInputStream objectInputStream;
    //to send file
    private static DataOutputStream dataOutputStream;
    //to download file
    private static DataInputStream dataInputStream;

    private static String session;
    private static ClientMessageListener clientMessageListener;

    ClientRequestSender(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String session) {
        ClientRequestSender.objectOutputStream = objectOutputStream;
        ClientRequestSender.objectInputStream = objectInputStream;
        ClientRequestSender.session = session;
    }

    public void run() {
        try {
            Socket fileSocket = new Socket("localhost", 5033);
            dataOutputStream = new DataOutputStream(fileSocket.getOutputStream());
            dataInputStream = new DataInputStream(fileSocket.getInputStream());
            dataOutputStream.writeUTF(session);
        } catch (IOException ignored) {
        }
        try {
            Socket chatSocket = new Socket("localhost", 5032);
            ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(chatSocket.getOutputStream());
            ObjectInputStream objectInputStream1 = new ObjectInputStream(chatSocket.getInputStream());
            objectOutputStream1.writeObject(session);
            clientMessageListener = new ClientMessageListener(objectInputStream1);
            Thread thread = new Thread(clientMessageListener);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        RequestType requestType = null;
        MenuType menuType = MenuType.MAIN_MENU;
        while (requestType == null || !(requestType.equals(RequestType.LOG_OUT))) {
            requestType = enterRequest(menuType);
            if (requestIsMenu(requestType)) {
                menuType = getMenu(requestType);
            } else if (requestType == RequestType.EXIT) {
                System.out.println(getString("Goodbye."));
                System.exit(0);
            } else if (requestType.equals(RequestType.BACK)) {
                assert menuType != null;
                menuType = backToPreviousMenu(menuType);
            } else {
                handleRequest(requestType, objectOutputStream, null);
                handleResponse(null, requestType, objectInputStream, objectOutputStream, null);
            }
        }
    }

    private MenuType backToPreviousMenu(MenuType menuType) {
        if (menuType.equals(MenuType.PENDING_MENU)) {
            return MenuType.FRIEND_MENU;
        } else if (menuType.equals(MenuType.FRIEND_MENU) || menuType.equals(MenuType.SERVER_MENU) || menuType.equals(MenuType.DIRECT_MESSAGE_MENU) || menuType.equals(MenuType.USER_SETTING_MENU)) {
            return MenuType.MAIN_MENU;
        } else {
            return null;
        }
    }

    private MenuType getMenu(RequestType requestType) {
        if (requestType == RequestType.USER_SETTING_MENU) {
            return MenuType.USER_SETTING_MENU;
        } else if (requestType == RequestType.SERVER_MENU) {
            return MenuType.SERVER_MENU;
        } else if (requestType == RequestType.FRIEND_MENU) {
            return MenuType.FRIEND_MENU;
        } else if (requestType == RequestType.DIRECT_MESSAGE_MENU) {
            return MenuType.DIRECT_MESSAGE_MENU;
        } else if (requestType == RequestType.PENDING) {
            return MenuType.PENDING_MENU;
        } else {
            return null;
        }
    }

    private boolean requestIsMenu(RequestType requestType) {
        return requestType == RequestType.FRIEND_MENU || requestType == RequestType.SERVER_MENU || requestType == RequestType.DIRECT_MESSAGE_MENU || requestType == RequestType.USER_SETTING_MENU || requestType == RequestType.PENDING;
    }

    public static String getString(String str) {
        return str;
    }

    public static RequestType enterRequest(MenuType menuType) {
        RequestType requestType = null;
        MenuFactory menuFactory = new MenuFactory();
        Menu menu = menuFactory.createMenu(menuType);
        String input;
        boolean check = false;
        while (!check) {
            try {
                System.out.println(menu.showMenu());
                input = getInput();
                requestType = menu.getMenuInputRequestType(Integer.parseInt(input));
                check = true;
            } catch (NumberFormatException e) {
                System.out.println(getString("invalid input!\nyou should enter an INTEGER number."));
            } catch (OutOFBoundOfMenuChoicesException ex) {
                System.out.println(ex.getMessage());
            }
        }
        return requestType;
    }


    public static void handleRequest(RequestType requestType, ObjectOutputStream objectOutputStream, String anID) {
        if (anID == null || anID.equals("")) {
            if (requestType.equals(RequestType.SIGN_UP)) {
                signUpRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.SIGN_IN)) {
                signInRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.EXIT)) {
                exitRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.LOG_OUT)) {
                logOutRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.SHOW_LIST_OF_SERVERS)) {
                showListOfServersRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.ADD_SERVER)) {
                addServerRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.MY_ACCOUNT)) {
                myAccountRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.CHANGE_USERNAME)) {
                changeUserNameRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.CHANGE_PASSWORD)) {
                changeUserPasswordRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.CHANGE_EMAIL)) {
                changeEmailRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.CHANGE_PROFILE)) {
                changeProfileRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.CHANGE_NUMBER_PHONE)) {
                changeNumberPhoneRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.ADD_FRIEND)) {
                addFriendRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.ALL_FRIENDS)) {
                allFriendRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.ONLINE_FRIENDS)) {
                onlineFriendRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.BLOCKED)) {
                blockedUsersRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.SHOW_DIRECT_MESSAGES)) {
                showDirectMessageRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.START_DIRECT_MESSAGE)) {
                startDirectMessage(objectOutputStream);
            } else if (requestType.equals(RequestType.SHOW_OUTGOING_FRIEND_REQUEST)) {
                showOutgoingFriendRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.SHOW_INCOMING_FRIEND_REQUEST)) {
                showIncomingFriendRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.CHANGE_NAME)) {
                changeNameRequest(objectOutputStream);
            } else if (requestType.equals(RequestType.SET_STATUS)) {
                setStatus(objectOutputStream);
            }
        } else {
            if (requestType.equals(RequestType.ADD_CHANNEL)) {
                addChannelRequest(objectOutputStream, anID);
            } else if (requestType.equals(RequestType.SEND_FRIEND_REQUEST)) {
                sendFriendRequest(objectOutputStream, anID);
            } else if (requestType.equals(RequestType.CHANGE_SERVER_NAME)) {
                changeServerNameRequest(objectOutputStream, anID);
            } else if (requestType.equals(RequestType.CREATE_ROLE)) {
                createRoleRequest(objectOutputStream, anID);
            } else if (requestType.equals(RequestType.EDIT_PERMISSIONS)) {
                editPermissions(objectOutputStream, anID);
            } else if (requestType.equals(RequestType.CHANGE_ROLE_NAME)) {
                changeRoleNameRequest(objectOutputStream, anID);
            } else if (requestType.equals(RequestType.ADD_MEMBER)) {
                addMemberToServerRequest(objectOutputStream, anID);
            } else {
                sendRequest(objectOutputStream, requestType, anID, null);
            }
        }
    }

    private static void sendFriendRequest(ObjectOutputStream objectOutputStream, String anID) {
        Scanner in = new Scanner(anID);
        String userID = "";
        while (in.hasNextLine()) {
            userID = in.nextLine();
        }
        sendRequest(objectOutputStream, RequestType.SEND_FRIEND_REQUEST, userID, null);
    }

    private static void changeRoleNameRequest(ObjectOutputStream objectOutputStream, String anID) {
        Scanner in = new Scanner(anID);
        String serverID = in.nextLine();
        String roleID = in.nextLine();
        System.out.println(getString("Enter a new name for the role : "));
        String newName = getInput();
        sendRequest(objectOutputStream, RequestType.CHANGE_ROLE_NAME, roleID + "\n" + newName, null);
    }

    private static void editPermissions(ObjectOutputStream objectOutputStream, String anID) {
        Scanner in = new Scanner(anID);
        String serverID = in.nextLine();
        String roleID = in.nextLine();
        HashMap<RequestType, Boolean> permissions = setPermissions();
        sendRequest(objectOutputStream, RequestType.EDIT_PERMISSIONS, roleID, permissions);
    }

    private static void createRoleRequest(ObjectOutputStream objectOutputStream, String anID) {
        System.out.println(getString("Enter a name for the role : "));
        String roleName = getInput();
        HashMap<RequestType, Boolean> permissions = setPermissions();
        sendRequest(objectOutputStream, RequestType.CREATE_ROLE, anID + "\n" + roleName, permissions);
    }

    private static HashMap<RequestType, Boolean> setPermissions() {
        HashMap<RequestType, Boolean> permissions = new HashMap<>();
        permissions.put(RequestType.ADD_CHANNEL, check("Create Channel\nthe user can add a channel to server.(y\\n)"));
        permissions.put(RequestType.REMOVE_CHANNEL, check("Remove Channel\nthe user can remove a channel from server.(y\\n)"));
        permissions.put(RequestType.REMOVE_MEMBER, check("Remove Member\nthe user can remove a member from server except owner.(y\\n)"));
        permissions.put(RequestType.LIMIT_MEMBERS_IN_CHANNEL, check("Limit members in a channel\nthe user can limit the members of a special channel.(y\\n)"));
        permissions.put(RequestType.BAN_MEMBER, check("Ban Members\nthe user can ban a member and the member access to only some special channels.(y\\n)"));
        permissions.put(RequestType.PIN_ONE_MESSAGE, check("Pin Message\nthe user can pin set pinned message in channels.(y\\n)"));
        permissions.put(RequestType.HISTORY_OF_CHAT, check("Access to history of chat\nthe user can access to history of all messages in text channels(y\\n)"));
        permissions.put(RequestType.CHANGE_SERVER_NAME, check("Change name of the server\nthe user can change the name of the server.(y\\n)"));
        return permissions;
    }

    private static boolean check(String text) {
        String input;
        System.out.println(text);
        input = getInput();
        while (!(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("n"))) {
            System.out.println("invalid input.");
            System.out.println("y or n");
            System.out.println(text);
            input = getInput();
        }
        return input.equalsIgnoreCase("y");
    }

    private static void changeServerNameRequest(ObjectOutputStream objectOutputStream, String anID) {
        System.out.println(getString("Enter a new name for server : "));
        String newName = getInput();
        sendRequest(objectOutputStream, RequestType.CHANGE_SERVER_NAME, anID + "\n" + newName, null);
    }

    private static void addMemberToServerRequest(ObjectOutputStream objectOutputStream, String anID) {
        System.out.println(getString("Enter username of a friend : "));
        String username = getInput();
        sendRequest(objectOutputStream, RequestType.ADD_MEMBER, anID + "\n" + username, null);
    }


    private static void addChannelRequest(ObjectOutputStream objectOutputStream, String serverID) {
        String toSend = "" + serverID;
        System.out.println(getString("Channel Name : "));
        String channelName = getInput();
        toSend += "\n" + channelName;
        ArrayList<String> channelTypes = new ArrayList<>();
        channelTypes.add("Text Channel");
        channelTypes.add("Voice Channel");
        String channelType = "";
        String input;
        while (channelType.equals("")) {
            showList(channelTypes);
            input = getInput();
            try {
                channelType = enterMenuChoice(channelTypes, Integer.parseInt(input));
            } catch (OutOFBoundOfMenuChoicesException e) {
                System.out.println(e.getMessage());
                channelType = "";
            } catch (NumberFormatException e) {
                System.out.println("invalid input. \n enter an INTEGER number.");
                channelType = "";
            }
        }
        toSend += "\n" + channelType;
        sendRequest(objectOutputStream, RequestType.ADD_CHANNEL, toSend, null);
    }

    private static void setStatus(ObjectOutputStream objectOutputStream) {
        ArrayList<UserStatus> statuses = new ArrayList<>();
        statuses.add(UserStatus.ONLINE);
        statuses.add(UserStatus.IDLE);
        statuses.add(UserStatus.DO_NOT_DISTURB);
        statuses.add(UserStatus.INVISIBLE);
        UserStatus choice = null;
        for (UserStatus it : statuses) {
            String temp = "" + it;
            System.out.println((statuses.indexOf(it) + 1) + " : " + temp.replace("_", " "));
        }
        String input;
        while (choice == null) {
            try {
                input = getInput();
                choice = enterUserStatus(statuses, Integer.parseInt(input));
            } catch (OutOFBoundOfMenuChoicesException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("invalid input. \n enter an INTEGER number.");
            }
        }
        sendRequest(objectOutputStream, RequestType.SET_STATUS, null, choice);
    }


    private static void changeNameRequest(ObjectOutputStream objectOutputStream) {
        String newName;
        System.out.println(getString("NEW NAME : "));
        newName = getInput();
        sendRequest(objectOutputStream, RequestType.CHANGE_NAME, newName, null);
    }

    private static void showIncomingFriendRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.SHOW_INCOMING_FRIEND_REQUEST, null, null);
    }

    private static void showOutgoingFriendRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.SHOW_OUTGOING_FRIEND_REQUEST, null, null);
    }

    private static void sendRequest(ObjectOutputStream objectOutputStream, RequestType requestType, String text, Object object) {
        Request request = new Request(requestType, text, object);
        try {
            objectOutputStream.writeObject(request);
        } catch (IOException e) {
            System.out.println("Some thing is wrong for server");
            e.printStackTrace();
        }
    }


    private static void startDirectMessage(ObjectOutputStream objectOutputStream) {
        StringBuilder result = new StringBuilder();
        String input;
        System.out.println("Enter the username of a friend : ");
        input = getInput();
        result.append(input);
        sendRequest(objectOutputStream, RequestType.START_DIRECT_MESSAGE, result.toString(), null);
    }

    private static void showDirectMessageRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.SHOW_DIRECT_MESSAGES, null, null);
    }

    private static void blockedUsersRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.BLOCKED, null, null);
    }


    private static void onlineFriendRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.ONLINE_FRIENDS, null, null);
    }

    private static void allFriendRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.ALL_FRIENDS, null, null);
    }

    private static void addFriendRequest(ObjectOutputStream objectOutputStream) {
        String userName;
        System.out.println(getString("Enter a UserName : "));
        userName = getInput();
        sendRequest(objectOutputStream, RequestType.ADD_FRIEND, userName, null);
    }

    private static void changeNumberPhoneRequest(ObjectOutputStream objectOutputStream) {
        String phoneNumber;
        phoneNumber = setNumberPhone("NEW NUMBER PHONE : ");
        String currentPassword;
        System.out.println(getString("CURRENT PASSWORD : "));
        currentPassword = getInput();
        sendRequest(objectOutputStream, RequestType.CHANGE_NUMBER_PHONE, currentPassword + "\n" + phoneNumber, null);
    }

    private static void changeProfileRequest(ObjectOutputStream objectOutputStream) {
        System.out.println("Enter the path of your image : ");
        String path = getInput();
        boolean check = false;
        while (!check || path.equals("#cancel")) {
            try {
                sendfile(path);
                check = true;
                sendRequest(objectOutputStream, RequestType.CHANGE_PROFILE, null, null);
                return;
            } catch (Exception e) {
                System.out.println("file not founded, enter a correct path. enter #cancel to cancel change profile : ");
                path = getInput();
            }
        }
        sendRequest(objectOutputStream, RequestType.TEMP, null, null);
    }

    private static void changeEmailRequest(ObjectOutputStream objectOutputStream) {
        String newEmail;
        String currentPassword;
        newEmail = setEmail("NEW EMAIL : ");
        System.out.println(getString("CURRENT PASSWORD : "));
        currentPassword = getInput();
        sendRequest(objectOutputStream, RequestType.CHANGE_EMAIL, currentPassword + "\n" + newEmail, null);
    }


    private static void changeUserPasswordRequest(ObjectOutputStream objectOutputStream) {
        String currentPassword;
        String newPassword;
        System.out.println(getString("CURRENT PASSWORD : "));
        currentPassword = getInput();
        newPassword = setPassword("NEW PASSWORD : ");
        sendRequest(objectOutputStream, RequestType.CHANGE_PASSWORD, currentPassword + "\n" + newPassword, null);
    }

    private static void changeUserNameRequest(ObjectOutputStream objectOutputStream) {
        String newUserName;
        String currentPassword;
        newUserName = setUsername("NEW USERNAME : ");
        System.out.println("CURRENT PASSWORD : ");
        currentPassword = getInput();
        sendRequest(objectOutputStream, RequestType.CHANGE_USERNAME, currentPassword + "\n" + newUserName, null);
    }

    private static void myAccountRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.MY_ACCOUNT, null, null);
    }

    private static void addServerRequest(ObjectOutputStream objectOutputStream) {
        System.out.println("Server Name : ");
        String serverName = getInput();
        sendRequest(objectOutputStream, RequestType.ADD_SERVER, serverName, null);
    }

    private static void showListOfServersRequest(ObjectOutputStream objectOutputStream) {
        Request request = new Request(RequestType.SHOW_LIST_OF_SERVERS, null, null);
        sendRequest(objectOutputStream, RequestType.SHOW_LIST_OF_SERVERS, null, null);
    }

    private static void logOutRequest(ObjectOutputStream objectOutputStream) {
        File file = new File("src\\Client\\session.txt");
        file.delete();
        sendRequest(objectOutputStream, RequestType.LOG_OUT, null, null);
    }

    public static void signUpRequest(ObjectOutputStream objectOutputStream) {
        String userName;
        String name;
        String password;
        String email;
        String phoneNumber;
        userName = setUsername("USERNAME : ");
        while (true) {
            System.out.println("NAME : ");
            name = getInput();
            if (name.equals("")) {
                System.out.println(getString("You must fill this field."));
            } else {
                break;
            }
        }
        password = setPassword("PASSWORD : ");
        email = setEmail("EMAIL : ");
        phoneNumber = setNumberPhone("NUMBER PHONE : (optional)");
        sendRequest(objectOutputStream, RequestType.SIGN_UP, userName + "\n" + name + "\n" + password + "\n" + email + "\n" + phoneNumber, null);
    }

    public static void signInRequest(ObjectOutputStream objectOutputStream) {
        String userName;
        String password;
        while (true) {
            System.out.println(getString("UserName : "));
            userName = getInput();
            if (userName.equals("")) {
                System.out.println(getString("You must fill this field."));
            } else {
                break;
            }
        }
        while (true) {
            System.out.println(getString("Password : "));
            password = getInput();
            if (password.equals("")) {
                System.out.println(getString("You must fill this field."));
            } else {
                break;
            }
        }

        sendRequest(objectOutputStream, RequestType.SIGN_IN, userName + "\n" + password, null);

    }

    public static void exitRequest(ObjectOutputStream objectOutputStream) {
        sendRequest(objectOutputStream, RequestType.EXIT, null, null);
        System.exit(0);
    }

    public static String setUsername(String text) {
        String userName = "";
        boolean check = false;
        boolean lengthIsOk;
        boolean justEnglishWordAndNumbers;
        while (!check) {
            System.out.println(getString(text));
            userName = getInput();
            try {
                lengthIsOk = lengthIsOk(userName, 6);
                justEnglishWordAndNumbers = justEnglishWordAndNumbers(userName);
                check = lengthIsOk && justEnglishWordAndNumbers;
            } catch (LengthException e) {
                System.out.println(e.getUsernameMessage());
            } catch (JustEnglishWordAndNumbersException e) {
                System.out.println(getString("userName must have an English word at least and must consist of numbers and English letters.\n(A-Z ,a-z, 0-9)"));
            }
        }
        return userName;
    }

    public static boolean justEnglishWordAndNumbers(String userName) throws JustEnglishWordAndNumbersException {
        boolean result;
        boolean check = true;
        int counter = 0;
        for (int i = 0; i < userName.length(); i++) {
            if (!(userName.charAt(i) >= 48 && userName.charAt(i) <= 57) && !(userName.charAt(i) >= 65 && userName.charAt(i) <= 90) && !(userName.charAt(i) >= 97 && userName.charAt(i) <= 122)) {
                counter++;
            }
        }
        if (counter > 0) {
            check = false;
        }
        result = (thereIsUpperCase(userName) || thereIsLowerCase(userName)) && check;
        return result;
    }

    public static String setPassword(String text) {
        String password = "";
        String confirmPassword;
        boolean lengthIsOk;
        boolean check = false;
        while (!check) {
            System.out.println(getString(text));
            password = getInput();
            System.out.println(getString("CONFIRM " + text));
            confirmPassword = getInput();
            try {
                check = confirmPassword(password, confirmPassword);
            } catch (PasswordsDontMatchException e) {
                System.out.println(e.getMessage());
                continue;
            }
            try {
                lengthIsOk = lengthIsOk(password, 8);
                check = check && checkSafetyOfPassword(password) && lengthIsOk;
            } catch (LengthException e) {
                System.out.println(e.getPasswordMessage());
                check = false;
            } catch (PasswordIsWeakException e) {
                System.out.println(e.getMessage());
                check = false;
            }
        }
        return password;
    }

    public static boolean confirmPassword(String password, String confirmPassword) throws PasswordsDontMatchException {
        if (password.equals(confirmPassword)) {
            return true;
        } else {
            throw new PasswordsDontMatchException();
        }
    }

    public static boolean checkSafetyOfPassword(String password) throws PasswordIsWeakException {
        boolean thereIsUpperCase;
        boolean thereIsLowerCase;
        boolean thereIsNumber;

        thereIsUpperCase = thereIsUpperCase(password);
        thereIsLowerCase = thereIsLowerCase(password);
        thereIsNumber = thereIsNumber(password);

        if (thereIsUpperCase && thereIsLowerCase && thereIsNumber) {
            return true;
        } else {
            throw new PasswordIsWeakException();
        }
    }

    public static boolean thereIsUpperCase(String str) {
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 65 && str.charAt(i) <= 90) {
                counter++;
            }
        }
        return counter > 0;
    }

    public static boolean thereIsNumber(String str) {
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                counter++;
            }
        }
        return counter > 0;
    }

    public static boolean thereIsLowerCase(String str) {
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 97 && str.charAt(i) <= 122) {
                counter++;
            }
        }
        return counter > 0;
    }

    public static boolean lengthIsOk(String str, int length) throws LengthException {
        if (str.length() >= length) {
            return true;
        } else {
            throw new LengthException();
        }
    }

    public static String getInput() {
        Scanner in = new Scanner(System.in);
        return in.nextLine();
    }

    public static String setEmail(String txt) {
        String email = "";
        String regex = "[A-Za-z0-9_\\-.]+@[A-Za-z0-9.]+\\.[A-Za-z]{2,4}";
        String[] regexes = new String[1];
        regexes[0] = regex;
        boolean check = false;
        while (!check) {
            System.out.println(getString(txt));
            email = getInput();
            try {
                check = checkRegex(regexes, email) && !(email.equals(""));
                if (email.equals("")) {
                    System.out.println("You must enter an email.");
                }
            } catch (InvalidRegexPatternException e) {
                System.out.println(getString("invalid email input."));
                System.out.println(getString("enter another one."));
            }
        }
        return email;
    }

    private static String setNumberPhone(String txt) {
        String phoneNumber = "";
        String regex1 = "\\+989[0-9]{9,9}";
        String regex2 = "09[0-9]{9,9}";
        String[] regexes = new String[2];
        regexes[0] = regex1;
        regexes[1] = regex2;
        boolean check = false;
        boolean condition1;
        while (!check) {
            System.out.println(getString(txt));
            phoneNumber = getInput();
            try {
                if (phoneNumber.equals("")) {
                    return "";
                }
                condition1 = checkRegex(regexes, phoneNumber);
                check = (condition1);
            } catch (InvalidRegexPatternException e) {
                System.out.println(getString("Invalid number phone"));
                System.out.println("correct numbers phone : \"+989123456789\" OR \"09123456789\"");
            }
        }
        return phoneNumber;
    }

    public static boolean checkRegex(String[] regex, String myString) throws InvalidRegexPatternException {
        myString = " " + myString + " ";
        Pattern pattern;
        Matcher matcher;
        for (String it : regex) {
            pattern = Pattern.compile(it);
            matcher = pattern.matcher(myString);
            while (matcher.find()) {
                if (matcher.group().length() != 0) {
                    return true;
                }
            }
        }
        throw new InvalidRegexPatternException();
    }

    private static void handleResponse(RequestType nextRequest, RequestType requestType, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, String choice) {
        if (RequestType.SHOW_DIRECT_MESSAGES == (requestType) || RequestType.ALL_FRIENDS == requestType || RequestType.ONLINE_FRIENDS == requestType || RequestType.BLOCKED == requestType || RequestType.SHOW_INCOMING_FRIEND_REQUEST == requestType || RequestType.SHOW_OUTGOING_FRIEND_REQUEST == requestType) {
            enterRequestFromSentList(requestType, RequestType.SHOW_USER, objectOutputStream, objectInputStream, "");
        } else if (RequestType.SHOW_LIST_OF_SERVERS == requestType) {
            enterRequestFromSentList(requestType, RequestType.SHOW_SERVER, objectOutputStream, objectInputStream, "");
        } else if (RequestType.LIST_OF_CHANNELS == requestType || RequestType.ROLES_SETTING == requestType) {
            ArrayList<RequestType> requestTypes = receiveRequestTypes(objectInputStream);
            while (true) {
                RequestType requestType1 = showRequestTypes(requestTypes);
                if (requestType1 == null) {
                    return;
                }
                if (requestType1.equals(RequestType.BACK)) {
                    return;
                }
                if (requestType1.equals(RequestType.EXIT)) {
                    System.out.println(getString("goodbye."));
                    System.exit(0);
                }
                handleRequest(requestType1, objectOutputStream, choice);
                handleResponse(null, requestType1, objectInputStream, objectOutputStream, choice);
            }
        } else if (RequestType.LIST_OF_ROLES == requestType) {
            enterRequestFromSentList(requestType, RequestType.SHOW_ROLE, objectOutputStream, objectInputStream, choice);
        } else if (RequestType.SHOW_MEMBERS_OF_CHANNEL == requestType) {
            enterRequestFromSentList(requestType, RequestType.SHOW_USER_IN_CHANNEL, objectOutputStream, objectInputStream, choice);
        } else if (RequestType.LIST_OF_MESSAGES == requestType) {
            String chatID = choice;
            choice = receiveContainerListResponse(objectInputStream);
            if (choice.equals("back")) {
                System.out.println("request has been canceled.");
                return;
            }
            sendRequest(objectOutputStream, nextRequest, chatID + "\n" + choice, null);
            handleResponse(null, nextRequest, objectInputStream, objectOutputStream, chatID + "\n" + choice);
        } else if (RequestType.LIST_OF_USERS == requestType) {
            enterRequestFromSentList(requestType, RequestType.SHOW_USER_IN_SERVER, objectOutputStream, objectInputStream, choice);
        } else if (RequestType.PIN_ONE_MESSAGE == requestType) {
            String serverAndChannelID = choice;
            choice = receiveContainerListResponse(objectInputStream);
            if (choice.equals("back")) {
                return;
            }
            sendRequest(objectOutputStream, RequestType.PIN, serverAndChannelID + "\n" + choice, null);
            handleResponse(null, RequestType.PIN, objectInputStream, objectOutputStream, serverAndChannelID + "\n" + choice);
        } else if (RequestType.REMOVE_ROLE_FROM_MEMBER == requestType) {
            String serverID = choice;
            choice = receiveContainerListResponse(objectInputStream);
            if (choice.equals("back")) {
                return;
            }
            sendRequest(objectOutputStream, RequestType.REMOVE_MEMBER_ROLE, serverID + "\n" + choice, null);
            handleResponse(null, RequestType.REMOVE_MEMBER_ROLE, objectInputStream, objectOutputStream, serverID + "\n" + choice);
        } else if (RequestType.ADD_ROLE_TO_MEMBER == requestType) {
            String serverID = choice;
            choice = receiveContainerListResponse(objectInputStream);
            if (choice.equals("back")) {
                return;
            }
            sendRequest(objectOutputStream, RequestType.ADD_ROLE, serverID + "\n" + choice, null);
            handleResponse(null, RequestType.ADD_ROLE, objectInputStream, objectOutputStream, serverID + "\n" + choice);
        } else if (RequestType.JOIN_VOICE_CALL == requestType || RequestType.JOIN_GROUP_VOICE_CALL == requestType) {
            Response response = receiveResponse(objectInputStream);
            try {
                startCall();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (RequestType.LIMIT_MEMBERS_IN_CHANNEL == requestType) {
            multiEnterFromSentList(RequestType.SET_MEMBERS_OF_CHANNEL, "LIMIT", "Limit the member of channel was canceled.", "enter option of member to add to channel members.", objectInputStream, objectOutputStream, choice);
        } else if (RequestType.BAN_MEMBER == requestType) {
            multiEnterFromSentList(RequestType.LIMIT_CHANNELS_OF_MEMBER, "BAN", "ban the member request has been canceled.", "enter the option of channel to add to member channels.", objectInputStream, objectOutputStream, choice);
        } else if (RequestType.LIST_OF_VOICE_CHANNELS == requestType || RequestType.LIST_OF_TEXT_CHANNELS == requestType) {
            enterRequestFromSentList(requestType, RequestType.SHOW_CHANNEL, objectOutputStream, objectInputStream, choice);
        } else if (RequestType.START_DIRECT_MESSAGE == requestType || RequestType.START_MESSAGING == requestType || RequestType.JOIN_CHAT == requestType) {
            Response response = receiveResponse(objectInputStream);
            boolean check = response.isSuccessful();
            if (check) {
                String chatID = response.text();
                ArrayList<String> lastMessages = (ArrayList<String>) response.object();
                if (lastMessages != null) {
                    for (String message : lastMessages) {
                        System.out.println(message);
                    }
                } else {
                    System.out.println("send first message!");
                }
                startMessaging(objectOutputStream, objectInputStream, chatID);
            } else {
                System.out.println(getString(response.text()));
            }
        } else {
            try {
                Response response = receiveResponse(objectInputStream);
                System.out.println(getString(response.text()));
            } catch (NullPointerException e) {
                //  e.printStackTrace();
            }

        }
    }

    private static void multiEnterFromSentList(RequestType requestType, String request, String backText, String text, ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream, String choice) {
        ArrayList<String> list;
        Response response = receiveResponse(objectInputStream);
        list = (ArrayList<String>) response.object();
        ArrayList<String> ID = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        if (list != null && list.size() != 0) {
            for (String it : list) {
                Scanner in = new Scanner(it);
                ID.add(in.nextLine());
                names.add(in.nextLine());
            }
        } else {
            System.out.println(getString("this list is empty."));
        }
        ID.add("back");
        names.add("BACK");
        String input;
        ArrayList<Integer> toSend = new ArrayList<>();
        while (true) {
            try {
                System.out.println(text);
                showList(names);
                input = getInput();
                String chose = (enterMenuChoice(ID, Integer.parseInt(input)));
                if (chose.equals("back")) {
                    System.out.println(backText);
                    return;
                }
                if (chose.equals(request)) {
                    break;
                }
                toSend.add(Integer.parseInt(chose));
                names.remove(ID.indexOf(chose));
                ID.remove(chose);
                names.remove("BACK");
                ID.remove("back");
                names.add(request);
                ID.add(request);
            } catch (OutOFBoundOfMenuChoicesException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println(getString("Invalid input.\nYou should enter an INTEGER number."));
            }
        }
        sendRequest(objectOutputStream, requestType, choice, toSend);
        handleResponse(null, requestType, objectInputStream, objectOutputStream, choice);
    }

    private static void enterRequestFromSentList(RequestType previousRequest, RequestType requestType, ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String serverID) {
        inner:
        while (true) {
            String choice;
            choice = receiveContainerListResponse(objectInputStream);
            if (choice.equalsIgnoreCase("back")) {
                return;
            }
            if (!serverID.equals("")) {
                choice = serverID + "\n" + choice;
            }
            while (true) {
                sendRequest(objectOutputStream, requestType, choice, null);
                ArrayList<RequestType> requestTypes = receiveRequestTypes(objectInputStream);
                RequestType requestType1 = showRequestTypes(requestTypes);
                if (requestType1 == null) {
                    handleRequest(previousRequest, objectOutputStream, serverID);
                    continue inner;
                }
                if (requestType1.equals(RequestType.BACK)) {
                    handleRequest(previousRequest, objectOutputStream, serverID);
                    continue inner;
                }
                if (requestType1.equals(RequestType.EXIT)) {
                    System.out.println(getString("goodbye."));
                    System.exit(0);
                }
                handleRequest(requestType1, objectOutputStream, choice);
                handleResponse(null, requestType1, objectInputStream, objectOutputStream, choice);
            }
        }
    }

    private static RequestType showRequestTypes(ArrayList<RequestType> requestTypes) {
        ArrayList<String> showingRequestType = new ArrayList<>();
        String input;
        RequestType requestType = null;
        if (requestTypes == null) {
            return null;
        }
        for (RequestType it : requestTypes) {
            String temp = "" + it;
            showingRequestType.add(temp.replace("_", " "));
        }
        boolean check = false;
        while (!check) {
            try {
                showList(showingRequestType);
                input = getInput();
                requestType = enterRequestMenuChoice(requestTypes, Integer.parseInt(input));
                check = true;
            } catch (OutOFBoundOfMenuChoicesException e) {
                System.out.println(e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println(getString("Invalid input.\nYou should enter an INTEGER number."));
            }
        }
        return requestType;
    }

    private static ArrayList<RequestType> receiveRequestTypes(ObjectInputStream objectInputStream) {
        Response response;
        ArrayList<RequestType> requestTypes = new ArrayList<>();
        try {
            response = (Response) objectInputStream.readObject();
            requestTypes = (ArrayList<RequestType>) response.object();
        } catch (IOException | ClassNotFoundException ignored) {
        }
        return requestTypes;
    }

    private static void startMessaging(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String chatID) {
        System.out.println(getString("enter #exit to quit the chat."));
        System.out.println(getString("enter #file to send file"));
        System.out.println(getString("enter #download to download file"));
        System.out.println(getString("enter #showpinned to see pinned message"));
        System.out.println(getString("enter #pin to pin a message"));
        System.out.println(getString("enter #like to like a message"));
        System.out.println(getString("enter #dislike to dislike a message"));
        System.out.println(getString("enter #laugh to laugh a message"));
        clientMessageListener.setOnlineChatID(chatID);
        String input = getInput();
        while (!input.equals("#exit")) {
            if (input.equals("")) {
                input = getInput();
                continue;
            }
            if (input.equals("#like")) {
                sendRequest(objectOutputStream, RequestType.LIST_OF_MESSAGES, chatID, null);
                handleResponse(RequestType.LIKE, RequestType.LIST_OF_MESSAGES, objectInputStream, objectOutputStream, chatID);
                input = getInput();
                continue;
            }
            if (input.equals("dislike")) {
                sendRequest(objectOutputStream, RequestType.LIST_OF_MESSAGES, chatID, null);
                handleResponse(RequestType.DISLIKE, RequestType.LIST_OF_MESSAGES, objectInputStream, objectOutputStream, chatID);
                input = getInput();
                continue;
            }
            if (input.equals("laugh")) {
                sendRequest(objectOutputStream, RequestType.LIST_OF_MESSAGES, chatID, null);
                handleResponse(RequestType.LAUGH, RequestType.LIST_OF_MESSAGES, objectInputStream, objectOutputStream, chatID);
                input = getInput();
                continue;
            }
            if (input.equals("#pin")) {
                sendRequest(objectOutputStream, RequestType.LIST_OF_MESSAGES, chatID, null);
                handleResponse(RequestType.PIN_IN_CHAT, RequestType.LIST_OF_MESSAGES, objectInputStream, objectOutputStream, chatID);
                input = getInput();
                continue;
            }
            if (input.equals("#showpinned")) {
                sendRequest(objectOutputStream, RequestType.SHOW_PINNED_MESSAGE, chatID, null);
                handleResponse(null, RequestType.SHOW_PINNED_MESSAGE, objectInputStream, objectOutputStream, null);
                input = getInput();
                continue;
            }
            if (input.equals("#file")) { // send file request
                System.out.println("Enter path of your file completely : ");
                String path = getInput();
                try {
                    sendfile(path);
                    sendRequest(objectOutputStream, RequestType.SEND_FILE, chatID, null);
                    handleResponse(null, RequestType.SEND_FILE, objectInputStream, objectOutputStream, "");
                    input = getInput();
                    continue;
                } catch (Exception e) {
                    System.out.println("file not founded.");
                    input = getInput();
                    continue;
                }
            } else if (input.equals("#download")) { // download file request
                System.out.println("Enter name of the file that you want to download : ");
                String fileName = getInput();
                try {
                    sendRequest(objectOutputStream, RequestType.RECEIVE_FILE, chatID, null);
                    receiveFile(fileName);
                    handleResponse(null, RequestType.RECEIVE_FILE, objectInputStream, objectOutputStream, "");
                    input = getInput();
                    continue;
                } catch (Exception e) {
                    e.printStackTrace();
                    input = getInput();
                    continue;
                }
            } else {
                sendRequest(objectOutputStream, RequestType.SEND_MESSAGE, chatID + "\n" + input, null);
            }
            try {
                objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            input = getInput();
        }
        clientMessageListener.setOnlineChatID("");
    }

    private static void sendfile(String path) throws Exception {
        int bytes;
        dataOutputStream.writeUTF(path.substring(path.lastIndexOf("\\") + 1));
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);
        // send file size
        dataOutputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }

    private static void startCall() {
        try {
            Socket socket = new Socket("localhost", 5050);
            AudioFormat format = new AudioFormat(192000.0f, 16, 2, true, false);
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            final DataLine.Info[] info = {new DataLine.Info(SourceDataLine.class, format)};
            SourceDataLine speakers = (SourceDataLine) AudioSystem.getLine(info[0]);
            speakers.open(format);
            speakers.start();
            // receive voice
            final boolean[] end = {false};
            Thread thread = new Thread(() -> {
                try {
                    int size;
                    byte[] data = new byte[1024];
                    while (!end[0]) {
                        size = dis.read(data);
                        speakers.write(data, 0, size);
                    }
                } catch (Exception e) {
                    end[0] = true;
                } finally {
                    speakers.drain();
                    speakers.close();
                }
            });
            thread.start();
            Thread thread1 = new Thread(() -> {
                TargetDataLine microphone = null;
                // send voice
                int size;
                byte[] data = new byte[1024];
                try {
                    microphone = AudioSystem.getTargetDataLine(format);
                    info[0] = new DataLine.Info(TargetDataLine.class, format);
                    microphone = (TargetDataLine) AudioSystem.getLine(info[0]);
                    microphone.open(format);
                    data = new byte[1024];
                    microphone.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                while (!end[0]) {
                    try {
                        assert microphone != null;
                        size = microphone.read(data, 0, 1024);
                        dos.write(data, 0, size);
                    } catch (IOException e) {
                        try {
                            microphone.close();
                            dos.close();
                            end[0] = true;
                        } catch (IOException f) {
                            f.printStackTrace();
                        }
                    }
                }
            });
            thread1.start();

            System.out.println("type #leave to quite voice call!");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            while (!input.equals("#leave")) {
                input = scanner.nextLine();
            }
            try {
                end[0] = true;
                dos.close();
                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (LineUnavailableException | IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(String fileName) throws Exception {
        int bytes;
        dataOutputStream.writeUTF(fileName);
        File file = new File("src\\ClientDatabase\\" + fileName); // where you want to save incoming file path
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        boolean check = dataInputStream.readBoolean();
        if (!check) {
            return;
        }
        long size = dataInputStream.readLong();     // read file size
        byte[] buffer = new byte[1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;      // read upto file size
        }
        fileOutputStream.close();
    }

    private static String receiveContainerListResponse(ObjectInputStream objectInputStream) {
        ArrayList<String> list;
        ArrayList<String> ID = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        String input;
        String choice = "";
        try {
            Response response = (Response) objectInputStream.readObject();
            list = (ArrayList<String>) response.object();
            System.out.println(response.text());
            if (list != null && list.size() != 0) {
                for (String it : list) {
                    Scanner in = new Scanner(it);
                    ID.add(in.nextLine());
                    names.add(in.nextLine());
                }
            } else {
                System.out.println(getString("this list is empty."));
            }
            ID.add("back");
            names.add("BACK");
            boolean check = false;
            while (!check) {
                try {
                    showList(names);
                    input = getInput();
                    choice = enterMenuChoice(ID, Integer.parseInt(input));
                    check = true;
                } catch (OutOFBoundOfMenuChoicesException e) {
                    System.out.println(e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println(getString("Invalid input.\nYou should enter an INTEGER number."));
                }
            }
        } catch (IOException | ClassNotFoundException | NullPointerException e) {
            e.printStackTrace();
        }
        return choice;
    }

    private static Response receiveResponse(ObjectInputStream objectInputStream) {
        Response response = null;
        try {
            response = (Response) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("some thing is wrong with  server.");
        }
        return response;
    }

    private static void showList(ArrayList<String> list) {
        for (String it : list) {
            System.out.println((list.indexOf(it) + 1) + " : " + getString(it));
        }
    }

    private static String enterMenuChoice(ArrayList<String> list, int input) throws OutOFBoundOfMenuChoicesException {
        if (input < 1 || input > list.size()) {
            throw new OutOFBoundOfMenuChoicesException();
        } else {
            return list.get(input - 1);
        }
    }

    private static RequestType enterRequestMenuChoice(ArrayList<RequestType> list, int input) throws OutOFBoundOfMenuChoicesException {
        if (input < 1 || input > list.size()) {
            throw new OutOFBoundOfMenuChoicesException();
        } else {
            return list.get(input - 1);
        }
    }

    private static UserStatus enterUserStatus(ArrayList<UserStatus> list, int input) throws OutOFBoundOfMenuChoicesException {
        if (input < 1 || input > list.size()) {
            throw new OutOFBoundOfMenuChoicesException();
        } else {
            return list.get(input - 1);
        }
    }
}