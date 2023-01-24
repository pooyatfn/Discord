package com.example.discord.model.Menu;

public class MenuFactory {
    public Menu createMenu(MenuType menuType) {
        if (menuType.equals(MenuType.FIRST_MENU)) {
            return new FirstMenu();
        } else if (menuType.equals(MenuType.FRIEND_MENU)) {
            return new FriendMenu();
        } else if (menuType.equals(MenuType.SERVER_MENU)) {
            return new ServerMenu();
        } else if (menuType.equals(MenuType.USER_SETTING_MENU)) {
            return new UserSettingMenu();
        } else if (menuType.equals(MenuType.DIRECT_MESSAGE_MENU)) {
            return new DirectMessageMenu();
        } else if (menuType.equals(MenuType.PENDING_MENU)) {
            return new PendingMenu();
        } else if (menuType.equals(MenuType.MAIN_MENU)) {
            return new MainMenu();
        }
        return null;
    }
}
