package Menu;

import model.RequestType;

public class UserSettingMenu extends Menu {

    UserSettingMenu() {
        super();
        setMenuType(MenuType.USER_SETTING_MENU);
    }

    @Override
    void setMenu() {
        getMenu().add(RequestType.MY_ACCOUNT);
        getMenu().add(RequestType.CHANGE_USERNAME);
        getMenu().add(RequestType.CHANGE_NAME);
        getMenu().add(RequestType.CHANGE_PROFILE);
        getMenu().add(RequestType.CHANGE_PASSWORD);
        getMenu().add(RequestType.CHANGE_EMAIL);
        getMenu().add(RequestType.CHANGE_NUMBER_PHONE);
        getMenu().add(RequestType.SET_STATUS);
        getMenu().add(RequestType.LOG_OUT);
        getMenu().add(RequestType.BACK);
        getMenu().add(RequestType.EXIT);
    }
}
