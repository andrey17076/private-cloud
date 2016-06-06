package by.bsuir.csan.client;

import by.bsuir.csan.client.gui.*;
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

    private static ClientSettings clientSettings;
    private ClientSession clientSession;

    private OptionButton syncingButton = new OptionButton("Turn on syncing", "Turn off syncing", false);
    private OptionButton overrideButton = new OptionButton("Turn on file override", "Turn off file override", false);

    public static void main(String[] args) {
        launch(args);
    }

    private String chooseShareableFolder(Stage stage) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File chosenDirectory = directoryChooser.showDialog(stage);

        try {
            clientSettings.setRootDir(chosenDirectory);
        } catch (IOException e) {
            AlertBox.display("Can't update config file");
        }
        clientSession.setClientSettings(clientSettings);

        return chosenDirectory.getPath();
    }

    private void handleSyncingButton() {
        if (syncingButton.isActive()) {
            try {
                clientSettings.setSyncingOption(false);
            } catch (IOException e) {
                AlertBox.display("Can't update config file");
            }
            clientSession.setClientSettings(clientSettings);
            AlertBox.display("Deactivated");
        } else {
            try {
                clientSettings.setSyncingOption(true);
            } catch (IOException e) {
                AlertBox.display("Can't update config file");
            }
            clientSession.setClientSettings(clientSettings);
            AlertBox.display("Activated");
        }
    }

    private void handleOverrideButton() {
        if (overrideButton.isActive()) {
            try {
                clientSettings.setOverrideOption(false);
            } catch (IOException e) {
                AlertBox.display("Can't update config file");
            }
            clientSession.setClientSettings(clientSettings);
            AlertBox.display("Deactivated");
        } else {
            try {
                clientSettings.setOverrideOption(true);
            } catch (IOException e) {
                AlertBox.display("Can't update config file");
            }
            clientSession.setClientSettings(clientSettings);
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
            clientSession = new ClientSession(clientSettings);
        } catch (IOException | NullPointerException e) {
            AlertBox.display("Can't connect to server!");
            primaryStage.close();
        }

        primaryStage.setOnCloseRequest(event -> {
            try {
                clientSession.quit();
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                try {
                    loginLabel.setText("You're not authorized");
                    syncSettingsVBox.setDisable(true);
                    clientSession.setClientSettings(new ClientSettings());
                } catch (IOException e) {
                    AlertBox.display("Can't log out");
                }
            } else {
                AccountBox.display("Log in");
                String login = AccountBox.getLogin();
                String passHash = AccountBox.getPassHash();
                if (login.equals("") | passHash.equals("")) {
                    logButton.setState(true);
                } else {
                    try {
                        clientSettings.setLogin(login);
                        clientSettings.setPassHash(passHash);
                        String response = clientSession.authorize(clientSettings);
                        if (response.equals("OK")) {
                            loginLabel.setText("Login: " + login);
                            clientSession.start();
                            syncSettingsVBox.setDisable(false);
                            AlertBox.display("Singed in");
                        } else {
                            AlertBox.display(response);
                            logButton.setState(true);
                        }
                    } catch (Exception e) {
                        AlertBox.display("Can't sign in!");
                        logButton.setState(true);
                    }
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
                    try {
                        String response = clientSession.signUp(login, passHash);
                        if (response.equals("OK")) {
                            AlertBox.display("Singed up");
                        } else {
                            AlertBox.display(response);
                        }
                    } catch (Exception e) {
                        AlertBox.display("Can't sign up!");
                    }
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
        try {
            clientSettings = ClientSettings.getClientSettings();

            if (clientSettings.getLogin() != null) {

                String response = clientSession.authorize(clientSettings);
                clientSession.start();
                if (response.equals("OK")) {
                    loginLabel.setText("Login: " + clientSettings.getLogin());
                    logButton.setState(true);
                    syncSettingsVBox.setDisable(false);
                    AlertBox.display("Singed in");
                }
            }

            syncingButton.setState(clientSettings.getSyncingOption());
            overrideButton.setState(clientSettings.getOverrideOption());
            syncDirPathTextField.setText(clientSettings.getRootDir().getPath());

        } catch (IOException | ClassNotFoundException e) {
            AlertBox.display("Can't load settings file");
        }
    }
}