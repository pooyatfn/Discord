package Menu;

import model.RequestType;

public class ServerMenu extends Menu {

    ServerMenu() {
        super();
        setMenuType(MenuType.SERVER_MENU);
    }

    @Override
    void setMenu() {
        getMenu().add(RequestType.SHOW_LIST_OF_SERVERS);
        getMenu().add(RequestType.ADD_SERVER);
        getMenu().add(RequestType.BACK);
        getMenu().add(RequestType.EXIT);
    }
}
