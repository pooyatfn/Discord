package Menu;

import java.util.ArrayList;

import Exception.OutOFBoundOfMenuChoicesException;
import model.RequestType;

public abstract class Menu {
    private final ArrayList<RequestType> menu;
    private MenuType menuType;

    public Menu() {
        menu = new ArrayList<>();
        this.setMenu();
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public void setMenuType(MenuType menuType) {
        this.menuType = menuType;
    }

    public ArrayList<RequestType> getMenu() {
        return menu;
    }

    public String showMenu() {
        StringBuilder result = new StringBuilder();
        for (RequestType it : menu) {
            result.append(menu.indexOf(it) + 1).append(" : ").append(requestTypeToString(it).replace("_", " ")).append("\n");
        }
        return result.toString();
    }

    private String requestTypeToString(RequestType requestType) {
        return "" + requestType;
    }

    abstract void setMenu();

    public RequestType getMenuInputRequestType(int input) throws OutOFBoundOfMenuChoicesException {
        if (input < 1 || input > getMenu().size()) {
            throw new OutOFBoundOfMenuChoicesException();
        } else {
            return getMenu().get(input - 1);
        }
    }
}
