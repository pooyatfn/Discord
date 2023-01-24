module com.example.discordfront {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.discord to javafx.fxml;
    exports com.example.discord;
    exports com.example.discord.controllers;
    opens com.example.discord.controllers to javafx.fxml;
}