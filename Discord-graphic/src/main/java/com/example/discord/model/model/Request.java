package com.example.discord.model.model;

import java.io.Serializable;

public record Request(RequestType type, String request, Object object) implements Serializable {
}