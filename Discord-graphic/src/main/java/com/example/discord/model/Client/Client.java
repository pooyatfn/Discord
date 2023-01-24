package com.example.discord.model.Client;

import com.example.discord.model.Menu.Menu;
import com.example.discord.model.Menu.MenuFactory;
import com.example.discord.model.Menu.MenuType;
import com.example.discord.model.model.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

public class Client implements Runnable{
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String session;

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectInputStream getObjectInputStream() {
        return objectInputStream;
    }

    public void setObjectInputStream(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    public void setObjectOutputStream(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }


    public void run() {

        while (true) {
            try {
                session = "";
                socket = new Socket("localhost", 5031);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
            } catch (ConnectException ex) {
                System.out.println("Server has not connected yet.");
                System.exit(0);
            } catch (SocketException e) {
                System.out.println("some thing is wrong with server.");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static String setSession(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream) {
        String session = "";
        RequestType requestType = RequestType.SIGN_IN;
        MenuFactory menuFactory = new MenuFactory();
        Menu menu = menuFactory.createMenu(MenuType.FIRST_MENU);
        boolean check;
        while (!(requestType.equals(RequestType.EXIT))) {
            try {
                while (session == null || session.equals("")) {
                    requestType = ClientRequestSender.enterRequest(menu.getMenuType());
                    ClientRequestSender.handleRequest(requestType, objectOutputStream, null);
                    Response response = (Response) objectInputStream.readObject();
                    System.out.println(response.text());
                    check = response.isSuccessful();
                    session = (String) response.object();
                    if (check && session != null && !session.equals("")) {
                        FileWriter fileWriter = new FileWriter("src\\Client\\session.txt");
                        fileWriter.write(session);
                        fileWriter.close();
                        return session;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
        return session;
    }

    private static boolean authentication(ObjectOutputStream objectOutputStream, ObjectInputStream objectInputStream, String session) {
        try {
            Request request = new Request(RequestType.AUTHENTICATION, session, null);
            objectOutputStream.writeObject(request);
            Response response = (Response) objectInputStream.readObject();
            return response.isSuccessful();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
}