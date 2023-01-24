package com.example.discord.model.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class ClientMessageListener implements Runnable {

    private String onlineChatID = "";
    private final ObjectInputStream objectInputStream1;

    ClientMessageListener(ObjectInputStream objectInputStream) {
        this.objectInputStream1 = objectInputStream;
    }

    public void setOnlineChatID(String onlineChatID) {
        this.onlineChatID = onlineChatID;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ArrayList<Object> receivedObject = (ArrayList<Object>) objectInputStream1.readObject();
                String chatID = (String) receivedObject.get(0);
                String message = (String) receivedObject.get(1);
                String senderUserName = (String) receivedObject.get(2);
                String receiverUserName = (String) receivedObject.get(3);
                String chatName = (String) receivedObject.get(4);
                if (onlineChatID.equals(chatID)) {
                    System.out.println(message);
                }
                if (message.contains("@" + receiverUserName)) {
                    System.out.println("Notification : " + senderUserName + " has mentioned you in a message in " + chatName);
                }
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("something is wrong with chat listener.");
                e.printStackTrace();
                break;
            }
        }
    }
}
