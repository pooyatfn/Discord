package Exception;

public class OutOFBoundOfMenuChoicesException extends Exception {

    @Override
    public String getMessage() {
        return "invalid input!\nenter an integer number according to the list.";
    }
}
