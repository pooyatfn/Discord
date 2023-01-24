package com.example.discord.model.Exception;

public class InvalidRegexPatternException extends Exception {
    @Override
    public String getMessage() {
        return "invalid  pattern.\nEnter another one.";
    }
}
