package com.example.discord;

import com.example.discord.model.Client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static Client client;

    public static Client getClient() {
        return client;
    }

    public static void setClient(Client client) {
        Main.client = client;
    }

    @Override
    public void start(Stage stage) throws IOException {
        client = new Client();
        Thread thread = new Thread(client);
        thread.start();
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("first-scene.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Discord");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
