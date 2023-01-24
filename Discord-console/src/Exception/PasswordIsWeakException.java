package Exception;

public class PasswordIsWeakException extends Exception {
    @Override
    public String getMessage() {
        return "Password is too weak or common to use.\nPassword must be have an upper case word, a lowercase word and an integer number.";
    }
}
