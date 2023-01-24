package model;

import java.io.Serializable;

public record Response(boolean isSuccessful, String text, Object object) implements Serializable {
}