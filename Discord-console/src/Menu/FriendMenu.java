package Menu;

import model.RequestType;

public class FriendMenu extends Menu {

    FriendMenu() {
        super();
        setMenuType(MenuType.FRIEND_MENU);
    }

    @Override
    void setMenu() {
        getMenu().add(RequestType.ONLINE_FRIENDS);
        getMenu().add(RequestType.ALL_FRIENDS);
        getMenu().add(RequestType.PENDING);
        getMenu().add(RequestType.ADD_FRIEND);
        getMenu().add(RequestType.BLOCKED);
        getMenu().add(RequestType.BACK);
        getMenu().add(RequestType.EXIT);
    }
}
