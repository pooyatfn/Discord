package Menu;

import model.RequestType;

public class MainMenu extends Menu {

    public MainMenu() {
        super();
        setMenuType(MenuType.MAIN_MENU);
    }

    void setMenu() {
        getMenu().add(RequestType.USER_SETTING_MENU);
        getMenu().add(RequestType.SERVER_MENU);
        getMenu().add(RequestType.FRIEND_MENU);
        getMenu().add(RequestType.DIRECT_MESSAGE_MENU);
        getMenu().add(RequestType.EXIT);
    }

}
