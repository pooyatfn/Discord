package Menu;

import model.RequestType;

public class FirstMenu extends Menu {

    FirstMenu() {
        super();
        setMenuType(MenuType.FIRST_MENU);
    }

    @Override
    void setMenu() {
        getMenu().add(RequestType.SIGN_UP);
        getMenu().add(RequestType.SIGN_IN);
        getMenu().add(RequestType.EXIT);
    }

}
