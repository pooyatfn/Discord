package server;

import database.DataHandler;
import database.FileDataHandler;
import model.chat.server.Role;

import java.util.HashSet;

public class RoleHandler {
    public static HashSet<Role> roles;
    public static DataHandler dataHandler;

    static {
        RoleHandler.dataHandler = new FileDataHandler();
        RoleHandler.roles = dataHandler.loadRoles();
    }

    public static Role getRole(int ID) {
        for (Role it : roles) {
            if (it.getID() == ID) {
                return it;
            }
        }
        return null;
    }

    public static void updateRoles() {
        RoleHandler.roles = dataHandler.loadRoles();
    }

}
