package com.example.discord.model.Menu;

import com.example.discord.model.model.RequestType;

public class PendingMenu extends Menu {

    PendingMenu() {
        super();
        setMenuType(MenuType.PENDING_MENU);
    }

    void setMenu() {
        getMenu().add(RequestType.SHOW_INCOMING_FRIEND_REQUEST);
        getMenu().add(RequestType.SHOW_OUTGOING_FRIEND_REQUEST);
        getMenu().add(RequestType.BACK);
        getMenu().add(RequestType.EXIT);
    }
}
