package Menu;

import model.RequestType;

public class DirectMessageMenu extends Menu {

    DirectMessageMenu() {
        super();
        setMenuType(MenuType.DIRECT_MESSAGE_MENU);
    }

    @Override
    void setMenu() {
        getMenu().add(RequestType.SHOW_DIRECT_MESSAGES);
        getMenu().add(RequestType.START_DIRECT_MESSAGE);
        getMenu().add(RequestType.BACK);
        getMenu().add(RequestType.EXIT);
    }
}
