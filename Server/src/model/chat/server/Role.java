package model.chat.server;

import model.RequestType;
import server.RoleHandler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Role implements Serializable {

    private final int ID;
    private String roleName;
    private HashMap<RequestType, Boolean> permissions;

    public int getID() {
        return ID;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public Role(String roleName, HashMap<RequestType, Boolean> permissions) {
        this.ID = RoleHandler.roles.size();
        this.roleName = roleName;
        this.permissions = permissions;
    }

    public ArrayList<RequestType> getShowServerPermissions() {
        ArrayList<RequestType> permissionsToReturn = new ArrayList<>();
        if (this.permissions.get(RequestType.ADD_CHANNEL)) {
            permissionsToReturn.add(RequestType.ADD_CHANNEL);
        }
        if (this.permissions.get(RequestType.CHANGE_SERVER_NAME)) {
            permissionsToReturn.add(RequestType.CHANGE_SERVER_NAME);
        }
        return permissionsToReturn;
    }

    public ArrayList<RequestType> getShowChannelPermissions() {
        ArrayList<RequestType> permissionsToReturn = new ArrayList<>();
        if (this.permissions.get(RequestType.REMOVE_CHANNEL)) {
            permissionsToReturn.add(RequestType.REMOVE_CHANNEL);
        }
        if (this.permissions.get(RequestType.HISTORY_OF_CHAT)) {
            permissionsToReturn.add(RequestType.HISTORY_OF_CHAT);
        }
        if (this.permissions.get(RequestType.PIN_ONE_MESSAGE)) {
            permissionsToReturn.add(RequestType.PIN_ONE_MESSAGE);
        }
        if (this.permissions.get(RequestType.LIMIT_MEMBERS_IN_CHANNEL)) {
            permissionsToReturn.add(RequestType.LIMIT_MEMBERS_IN_CHANNEL);
        }
        return permissionsToReturn;
    }

    public ArrayList<RequestType> getShowUserPermission() {
        ArrayList<RequestType> permissionsToReturn = new ArrayList<>();
        if (this.permissions.get(RequestType.REMOVE_MEMBER)) {
            permissionsToReturn.add(RequestType.REMOVE_MEMBER);
        }
        if (this.permissions.get(RequestType.BAN_MEMBER)) {
            permissionsToReturn.add(RequestType.BAN_MEMBER);
        }
        return permissionsToReturn;
    }

    public String showPermissions() {
        StringBuilder result = new StringBuilder();
        for (RequestType it : permissions.keySet()) {
            if (permissions.get(it)) {
                result.append(it.toString().replace("_", " ")).append(" : YES").append("\n");
            } else {
                result.append(it.toString().replace("_", " ")).append(" : NO").append("\n");
            }
        }
        return result.toString();
    }

    public void editPermissions(HashMap<RequestType, Boolean> permissions) {
        this.permissions = permissions;
    }
}
