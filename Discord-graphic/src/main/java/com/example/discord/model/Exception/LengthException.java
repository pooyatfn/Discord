package com.example.discord.model.Exception;

public class LengthException extends Exception {
    public String getPasswordMessage() {
        return "Password must be 8 or more length.";
    }

    public String getUsernameMessage() {
        return "Username must be 6 or more length";
    }
}
