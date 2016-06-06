package by.bsuir.csan.client;

import by.bsuir.csan.client.gui.*;
import by.bsuir.csan.client.settings.ClientSettingsManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;

public class Client extends Application {

    private ClientSession clientSession;

    private OptionButton syncingButton = new OptionButton("Turn on syncing", "Turn off syncing", false);
    private OptionButton overrideButton = new OptionButton("Turn on file override", "Turn off file override", false);

    public static void main(String[] args) {
        ClientSettingsManager.loadSettingsFromFile();
        launch(args);
    }

    private String chooseShareableFolder(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File chosenDirectory = directoryChooser.showDialog(stage);
        ClientSettingsManager.setRootDir(chosenDirectory);
        return chosenDirectory.getPath();
    }

    private void handleSyncingButton() {
        if (syncingButton.isActive()) {
            ClientSettingsManager.setSyncingOption(false);
            AlertBox.display("Deactivated");
        } else {
            ClientSettingsManager.setSyncingOption(true);
            AlertBox.display("Activated");
        }
    }

    private void handleOverrideButton() {
        if (overrideButton.isActive()) {
            ClientSettingsManager.setOverrideOption(false);
            AlertBox.display("Deactivated");
        } else {
            ClientSettingsManager.setOverrideOption(true);
            AlertBox.display("Activated");
        }
    }

    @Override
    public void start(Stage primaryStage) {

        //Main pane
        VBox mainPane = new VBox(15);
        mainPane.setPadding(new Insets(15));
        mainPane.setPrefWidth(Region.USE_COMPUTED_SIZE);

        Scene mainScene = new Scene(mainPane);
        primaryStage.setMinWidth(500);
        primaryStage.setMinHeight(250);
        primaryStage.setMaxHeight(250);
        primaryStage.setTitle("Private Cloud");
        primaryStage.setScene(mainScene);
        primaryStage.show();

        /**
         * Try to establish connection with server and start client sessions.
         * In a case, when server is unavailable, app will be closed due to it's useless.
         */
        try {
            clientSession = new ClientSession();
        } catch (IOException e) {
            AlertBox.display("Can't connect to server!");
            primaryStage.close();
        }

        primaryStage.setOnCloseRequest(event -> {
            clientSession.quit();
            clientSession.closeSession();
        });

        //Syncing Settings Titled VBox
        TitledVBox syncSettingsVBox = new TitledVBox("Sharing Settings");
        syncSettingsVBox.setDisable(true);

        TextField syncDirPathTextField = new TextField();
        syncDirPathTextField.setEditable(false);
        HBox.setHgrow(syncDirPathTextField, Priority.ALWAYS);

        Button syncDirChooseButton = new Button("Choose");
        syncDirChooseButton.setOnAction(event -> syncDirPathTextField.setText(chooseShareableFolder(primaryStage)));

        syncingButton.setOnActivation(event -> handleSyncingButton());
        overrideButton.setOnActivation(event -> handleOverrideButton());

        syncSettingsVBox.addAll(
                new HBox(15, syncDirPathTextField, syncDirChooseButton),
                new HBox(15, syncingButton, overrideButton)
        );
        //End of Syncing Setting Titled VBox

        //Account Setting Titled HBox
        TitledHBox accountSettingsTitleHBox = new TitledHBox("Account Settings");

        Label loginLabel = new Label("You're not authorized");
        loginLabel.setPrefHeight(27);

        OptionButton logButton = new OptionButton("Log in", "Log out", false);
        logButton.setOnActivation(event -> {
            if (logButton.isActive()) {
                loginLabel.setText("You're not authorized");
                syncSettingsVBox.setDisable(true);
            } else {
                AccountBox.display("Log in");
                String login = AccountBox.getLogin();
                String passHash = AccountBox.getPassHash();
                if (login.equals("") | passHash.equals("")) {
                    logButton.setState(true);
                } else {
                    ClientSettingsManager.setLoginInfo(login, passHash);
                    String response = clientSession.authorize();
                    if (response.equals("DONE")) {
                        loginLabel.setText("Login: " + login);
                        syncSettingsVBox.setDisable(false);
                    } else {
                        logButton.setState(true);
                    }
                    AlertBox.display(response);
                }
            }
        });

        Button signUpButton = new Button("Sign up");
        signUpButton.setOnAction(event -> {
            AccountBox.display(signUpButton.getText());
            String login = AccountBox.getLogin();
            String passHash = AccountBox.getPassHash();
            if (login.equals("") | passHash.equals("")) {
                //user canceled account box
            } else {
                String response = clientSession.signUp();
                AlertBox.display(response);
            }
        });

        Region emptySpace = new Region();
        HBox.setHgrow(emptySpace, Priority.ALWAYS);

        accountSettingsTitleHBox.addAll(loginLabel, emptySpace, logButton, signUpButton);
        //End of Account Setting Titled HBox

        mainPane.getChildren().addAll(accountSettingsTitleHBox, syncSettingsVBox);
        //End of Main Layout

        /**
         * Try to get client's settings of app.
         * If file with settings can't be loaded, we use default ones.
         */
        ClientSettingsManager.loadSettingsFromFile();

        if (ClientSettingsManager.getLogin() != null) {

            String response = clientSession.authorize();
            if (response.equals("DONE")) {
                loginLabel.setText("Login: " + ClientSettingsManager.getLogin());
                logButton.setState(true);
                syncSettingsVBox.setDisable(false);
            }
        }

        syncingButton.setState(ClientSettingsManager.getSyncingOption());
        overrideButton.setState(ClientSettingsManager.getOverrideOption());
        syncDirPathTextField.setText(ClientSettingsManager.getRootDir().getPath());
        clientSession.start();
    }
}