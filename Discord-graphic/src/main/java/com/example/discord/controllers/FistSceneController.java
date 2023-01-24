package com.example.discord.controllers;

import com.example.discord.Main;
import com.example.discord.model.Client.ClientRequestSender;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.example.discord.model.model.RequestType;
import com.example.discord.model.model.Response;
import java.io.*;

public class FistSceneController {
    private File profileFile;
    @FXML
    private AnchorPane sceneAnchorPane;
    @FXML
    private Pane signInPane;
    @FXML
    private Label signInLabel;
    @FXML
    private TextField signInUsername;
    @FXML
    private TextField signInPassword;
    @FXML
    private Pane signUpPane;
    @FXML
    private TextField signUpUsername;
    @FXML
    private TextField signUpPassword;
    @FXML
    private TextField signUpEmail;
    @FXML
    private TextField signUpFullName;
    @FXML
    private Pane secondSignUpPane;
    @FXML
    private TextField signUpPhoneNumber;
    @FXML
    private Label signUpStatus;
    @FXML
    private TextField profilePath;
    @FXML
    private ChoiceBox<String> status;

    private void signInPane() {
        signUpPane.setVisible(false);
        secondSignUpPane.setVisible(false);
        signInPane.setVisible(true);
    }

    private void signUpPane() {
        signUpPane.setVisible(true);
        secondSignUpPane.setVisible(false);
        signInPane.setVisible(false);
    }

    private void secondSignUpPane() {
        signUpPane.setVisible(false);
        secondSignUpPane.setVisible(true);
        signInPane.setVisible(false);
        status.setItems(FXCollections.observableArrayList("Online", "Idle", "Do Not Disturb", "Invisible"));
        status.setValue("Online");
    }

    private void applicationPane() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("applicationScene.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("Discord");
            stage.setScene(scene);
            Stage s = (Stage) sceneAnchorPane.getScene().getWindow();
            s.close();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void loginButtonClick() {
        String username = signInUsername.getText();
        String password = signInPassword.getText();
        ClientRequestSender.sendRequest(Main.getClient().getObjectOutputStream(), RequestType.SIGN_IN,username+"\n"+password,null);
        Response response = ClientRequestSender.receiveResponse(Main.getClient().getObjectInputStream());
        if (!response.isSuccessful()) { // if wrong
            signInPassword.clear();
            signInUsername.clear();
            signInLabel.setText("username or password is wrong!");
        } else {
            Main.getClient().setSession((String)response.object());
            applicationPane();
        }
    }

    @FXML
    private void registerAccountHyperlinkClick() {
        signUpPane();
    }

    @FXML
    private void createAccountButtonClick() {
        String username = signUpUsername.getText();
        String password = signUpPassword.getText();
        String email = signUpEmail.getText();
        String name = signUpFullName.getText();
        ClientRequestSender.sendRequest(Main.getClient().getObjectOutputStream(), RequestType.SIGN_UP,username+"\n"+name+"\n"+password+"+"+email,null);
        Response response = ClientRequestSender.receiveResponse(Main.getClient().getObjectInputStream());
        if (!response.isSuccessful()) { // if wrong
            signUpPassword.clear();
            signUpUsername.clear();
        } else {
            // create account
            secondSignUpPane();
        }
    }

    @FXML
    private void haveAccountHyperlinkClick() {
        signInPane();
    }

    @FXML
    private void browseButtonClick() {
        FileChooser chooser = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg");
        chooser.getExtensionFilters().add(extensionFilter);
        File file = chooser.showOpenDialog(sceneAnchorPane.getScene().getWindow());
        if (file != null) {
            profilePath.setText(file.getAbsolutePath());
            profileFile = file;
        }
    }

    @FXML
    private void continueButtonClick() {
        String phoneNumber;
        if (signUpPhoneNumber != null) {
            phoneNumber = signUpPhoneNumber.getText();
        }
        String status = signUpStatus.getText();
        // profileFile;  // delete comment

        if (false) {

        } else {
            signInPane();
        }
    }

    @FXML
    private void backHyperlinkClick() {
        signUpPane();
    }
}