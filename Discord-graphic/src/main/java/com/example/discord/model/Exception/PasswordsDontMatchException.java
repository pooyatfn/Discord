package com.example.discord.model.Exception;

public class PasswordsDontMatchException extends Exception {
    @Override
    public String getMessage() {
        return "Passwords dont match.";
    }
}
