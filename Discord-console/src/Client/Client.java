package Client;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

import Menu.*;
import Menu.MenuFactory;
import model.*;

public class Client {

    public static void main(String[] args) {
        Socket socket;
        ObjectInputStream objectInputStream;
        ObjectOutputStream objectOutputStream;
        StringBuilder session;
        while (true) {
            try {
                session = new StringBuilder();
                socket = new Socket("localhost", 5031);
                objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectInputStream = new ObjectInputStream(socket.getInputStream());
                try {
                    FileReader fileReader = new FileReader("src\\Client\\session.txt");
                    int i;
                    while ((i = fileReader.read()) != -1) {
                        session.append((char) i);
                    }
                    fileReader.close();
                } catch (FileNotFoundException ex) {
                    //System.out.println("file not founded.");
                }
                boolean check = false;
                while (!check) {
                    if (session.toString().equals("")) {
                        session = new StringBuilder(setSession(objectOutputStream, objectInputStream));
                        check = true;
                    } else {
                        check = authentication(objectOutputStream, objectInputStream, session.toString());
                        if (!check) {
                            session = new StringBuilder();
                        }
                    }
                }
                ClientRequestSender clientRequestSender = new ClientRequestSender(objectOutputStream, objectInputStream, session.toString());
                clientRequestSender.run();
                objectInputStream.close();
                objectOutputStream.close();
                socket.close();

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