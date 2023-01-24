package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Objects;

public class Server {

    private final ServerSocket serverSocket;
    private final ServerSocket chatSocket;
    private final ServerSocket fileSocket;
    private final ServerSocket callSocket;

    HashMap<DataOutputStream, DataInputStream> listeners = new HashMap<>();
    Socket listener;
    DataOutputStream dos;
    DataInputStream dis;
    DataOutputStream listen;

    public Server(ServerSocket serverSocket, ServerSocket chatSocket, ServerSocket fileSocket, ServerSocket callSocket) {
        this.serverSocket = serverSocket;
        this.chatSocket = chatSocket;
        this.fileSocket = fileSocket;
        this.callSocket = callSocket;
    }

    public void startServer() {
        try {
            // Listen for connections (clients to connect) on port 1234.
            while (!serverSocket.isClosed()) {
                // Will be closed in the Client Handler.
                System.out.println("server is listening.");
                Socket socket = serverSocket.accept();
                System.out.println("A new client has connected!");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                // The start method begins the execution of a thread.
                // When you call start() the run method is called.
                // The operating system schedules the threads.
                thread.start();
            }
        } catch (IOException | ClassNotFoundException e) {
            closeServerSocket();
            e.printStackTrace();
        }
    }

    public void chatServer() {
        Thread thread = new Thread(() -> {
            try {
                // Listen for connections (clients to connect) on port 5032.
                while (!chatSocket.isClosed()) {
                    // Will be closed in the Client Handler.
                    Socket socket = chatSocket.accept();
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                    String session = (String) objectInputStream.readObject();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    Objects.requireNonNull(ClientHandler.getClientHandler(Objects.requireNonNull(ClientHandler.getUser(session)).getID())).setChatSocket(objectOutputStream);
                }
            } catch (IOException | ClassNotFoundException e) {
                closeChatServerSocket();
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void fileServer() {
        Thread thread = new Thread(() -> {
            try {
                // Listen for connections (clients to connect) on port 5033.
                while (!fileSocket.isClosed()) {
                    // Will be closed in the Client Handler.
                    Socket socket = fileSocket.accept();
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                    String session = dataInputStream.readUTF();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ignored) {
                    }
                    Objects.requireNonNull(ClientHandler.getClientHandler(session)).setFileSocket(dataInputStream, dataOutputStream);
                }
            } catch (IOException e) {
                closeFileServerSocket();
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void callServer() {
        Thread thread = new Thread(() -> {
            try {
                System.out.println("Server Started");
                while (!callSocket.isClosed()) {
                    listener = callSocket.accept();
                    dos = new DataOutputStream(listener.getOutputStream());
                    dis = new DataInputStream(listener.getInputStream());
                    listeners.put(dos, dis);
                    System.out.println("Connected from [" + listener.getPort() + " : " + listener.getInetAddress() + "]");
                    System.out.println("Current listener : " + listeners.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeChatServerSocket() {
        try {
            if (chatSocket != null) {
                chatSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFileServerSocket() {
        try {
            if (fileSocket != null) {
                fileSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadCast() {
        new Thread(() -> {
            int size;
            byte[] data = new byte[1024];
            while (true) {
                try {
                    for (DataInputStream dataInputStream : listeners.values()) {
                        size = dataInputStream.read(data, 0, 1024);
                        for (DataOutputStream dos : listeners.keySet()) {
                            listen = dos;
                            if (listeners.get(listen) == dataInputStream) {
                                continue;
                            }
                            listen.write(data, 0, size);
                        }
                    }
                } catch (IndexOutOfBoundsException | IOException e) {
                    try {
                        if (listen != null) {
                            listen.close();
                        }
                        listeners.remove(listen);
                        System.out.println("Someone Disconnected");
                        System.out.println("Current listener : " + listeners.size());
                    } catch (IOException f) {
                        f.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5031);
        ServerSocket chatSocket = new ServerSocket(5032);
        ServerSocket fileSocket = new ServerSocket(5033);
        ServerSocket callSocket = new ServerSocket(5050);
        Server server = new Server(serverSocket, chatSocket, fileSocket, callSocket);
        server.fileServer();
        server.chatServer();
        server.callServer();
        server.broadCast();
        server.startServer();
    }
}