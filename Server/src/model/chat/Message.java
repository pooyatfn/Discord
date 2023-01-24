package model.chat;

import server.ClientHandler;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class Message implements Serializable {

    private final int senderUser;
    private final String body;
    private final Message repliedTo;
    private final ArrayList<ReActionType> reActions = new ArrayList<>();
    private final LocalDateTime dateTime;

    public Message(int senderUser, String body, Message repliedTo) {
        this.senderUser = senderUser;
        this.body = body;
        this.repliedTo = repliedTo;
        this.dateTime = LocalDateTime.now();
    }

    public ArrayList<ReActionType> getReActions() {
        return reActions;
    }

    @Override
    public String toString() {
        StringBuilder message = new StringBuilder();
        if (repliedTo != null) {
            String reply;
            if (repliedTo.body.length() <= 10) {
                reply = repliedTo.body;
            } else {
                reply = repliedTo.body.substring(0, 11) + "...";
            }
            message.append("replied to: ").append(reply).append('\n');
        }
        message.append(Objects.requireNonNull(ClientHandler.getUser(senderUser)).getName());
        message.append(" : ").append(body);
        int length = message.length();
        message.append(" ".repeat(Math.max(0, 100 - length)));
        message.append(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        if (!reActions.isEmpty()) {
            int likes = 0, dislikes = 0, laugh = 0;
            for (ReActionType reActionType : reActions) {
                if (reActionType == ReActionType.LIKE) {
                    likes++;
                } else if (reActionType == ReActionType.LAUGH) {
                    laugh++;
                } else if (reActionType == ReActionType.DISLIKE) {
                    dislikes++;
                }
            }
            message.append('\n');
            if (likes != 0) {
                message.append("likes : ").append(likes).append(" ");
            }
            if (dislikes != 0) {
                message.append("dislikes : ").append(dislikes).append(" ");
            }
            if (laugh != 0) {
                message.append("laughs : ").append(laugh).append(" ");
            }
        }
        return message.toString();
    }

    public int getSenderUser() {
        return senderUser;
    }
}