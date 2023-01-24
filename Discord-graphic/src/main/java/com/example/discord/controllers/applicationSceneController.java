package com.example.discord.controllers;

import com.example.discord.Main;
import com.example.discord.model.Client.ClientRequestSender;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.example.discord.model.model.RequestType;
import com.example.discord.model.model.Response;
import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class applicationSceneController implements Initializable {
    private String session;
    private int friendID; // witch friend we selected
    private int serverID; // witch server we selected
    private int channelID; // witch channel we selected
    @FXML
    private VBox servers;
    @FXML
    private Label information;
    @FXML
    private Pane homeSidePane;
    @FXML
    private Pane serverPane;
    @FXML
    private Label serverNameLabel;
    @FXML
    private Pane friendsPane;
    @FXML
    private VBox directsVBox;
    @FXML
    private VBox channelsVBox;
    @FXML
    private Label chatName;
    @FXML
    private VBox chatVBox;
    @FXML
    private TextField text;
    @FXML
    private VBox chatMembers;
    private Image image;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void addServer() {
        // send create-server request and get serverID
        String serverName = "mammad";
        ClientRequestSender.sendRequest(Main.getClient().getObjectOutputStream(), RequestType.ADD_SERVER,serverName,null);
        Response response = ClientRequestSender.receiveResponse(Main.getClient().getObjectInputStream());
        int serverID = (Integer) response.object();
        Button button = serverButtonCreator(serverID);
        HBox hBox = new HBox();
        hBox.getChildren().add(button);
        servers.getChildren().add(hBox);
    }

    @FXML
    private void home() {
        //
    }

    @FXML
    private void showUserInfo() {
        //
    }

    @FXML
    private void startDirectVoice() {
        //
    }

    @FXML
    private void directClick() {
        Button button = directMessageButtonCreator(5);
        HBox hBox = new HBox();
        hBox.getChildren().add(button);
        channelsVBox.getChildren().add(hBox);
    }

    private Button serverButtonCreator(int serverID) {
        ImageView imageView; // get image of the server from server
        Button button = new Button();
        button.setMinSize(50, 50);
        button.setMaxSize(50, 50);
        //imageView.setFitHeight(50);
        //imageView.setFitWidth(50);
        //button.setGraphic(imageView);
        button.setOnAction(event -> {
            // send get-server request
        });
        return button;
    }

    private Button directMessageButtonCreator(int directID) {
        ImageView imageView; // get image of the user from server
        Button button = new Button();
        button.setMinSize(220, 35);
        button.setMaxSize(220, 35);
        String name = "iman"; // get name of the user from server
        button.setText(name);
        button.setOnAction(event -> {
            // set things on direct
        });
        return button;
    }

    @FXML
    private void onlineFriends() {
        //
    }

    @FXML
    private void serverSettings() {
        //
    }

    @FXML
    private void settings() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("user-settings.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Stage stage = new Stage();
        stage.setTitle("Settings");
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    @FXML
    private void addDirect() {
        //
    }

    @FXML
    private void startGroupVoiceChat() {
        //
    }

    @FXML
    private void showChannelPinnedMessage() {
        //
    }

    @FXML
    private void sendMessage() {
        String message = text.getText();
        text.clear();
        // sent message to the model
    }

    private void addToChatVBox(String message) {
        //
    }

    private void showChatMembers() {
        //
    }
}
