package model;

import server.ClientHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class User implements Serializable {

    private final int ID;
    private String name;
    private String userName;
    private String password;
    private String email;
    private String phoneNumber;
    private String imageName;
    private UserStatus status;
    private final ArrayList<Integer> friends = new ArrayList<>();
    private final ArrayList<Integer> directMessages = new ArrayList<>();
    private final ArrayList<Integer> discordServers = new ArrayList<>();
    private final ArrayList<Integer> friendRequests = new ArrayList<>();
    private final ArrayList<Integer> sentFriendRequests = new ArrayList<>();
    private final ArrayList<Integer> blockedUsers = new ArrayList<>();

    public User(String userName, String name, String password, String email, String phoneNumber, String imageName) {
        this.ID = ClientHandler.users.size();
        this.userName = userName;
        this.name = name;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.imageName = imageName;
        this.status = UserStatus.OFFLINE;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Integer> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public int getID() {
        return ID;
    }

    public String getUserName() {
        return userName;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void addToFriendRequest(int userID) {
        friendRequests.add(userID);
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void acceptFriendRequest(int userID) {
        friendRequests.remove((Integer) userID);
        sentFriendRequests.remove((Integer) userID);
        friends.add(userID);
    }

    public void removeFriend(int userID) {
        friends.remove((Integer) userID);
    }

    public String getImage() {
        return imageName;
    }

    public void setImage(String image) {
        this.imageName = image;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void cancelFriendRequest(int userID) {
        sentFriendRequests.remove((Integer) userID);
        Objects.requireNonNull(ClientHandler.getUser(userID)).removeFriendRequestFromOthers(getID());
    }

    public void removeFriendRequestFromOthers(int userID) {
        friendRequests.remove((Integer) userID);
    }

    public ArrayList<Integer> getFriends() {
        return friends;
    }

    public ArrayList<Integer> getDirectMessages() {
        return directMessages;
    }

    public ArrayList<Integer> getDiscordServers() {
        return discordServers;
    }

    public ArrayList<Integer> getFriendRequests() {
        return friendRequests;
    }

    public ArrayList<Integer> getBlockedUsers() {
        return blockedUsers;
    }

    public void rejectFriendRequest(int userID) {
        friendRequests.remove((Integer) userID);
    }

    public void blockUser(int userID) {
        blockedUsers.add(userID);
        friends.remove((Integer) userID);
        friendRequests.remove((Integer) userID);
        sentFriendRequests.remove((Integer) userID);
    }

    public void unblockUser(int userID) {
        blockedUsers.remove((Integer) userID);
    }

    public boolean isBlocked(int userID) {
        for (Integer it : blockedUsers) {
            if (it == userID) {
                return true;
            }
        }
        return false;
    }

    public boolean isFriend(int userID) {
        for (Integer it : friends) {
            if (it == userID) {
                return true;
            }
        }
        return false;
    }

    public boolean isFriendRequesting(int userID) {
        for (Integer it : sentFriendRequests) {
            if (it == userID) {
                return true;
            }
        }
        return false;
    }

    public boolean isFriendRequested(int userID) {
        for (Integer it : friendRequests) {
            if (it == userID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userName.equals(user.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

    @Override
    public String toString() {
        return name + '\n' + "userName : " + userName + '\n' + "email : " + email + '\n' + "phoneNumber : " + phoneNumber + '\n' + "status : " + status;
    }
}