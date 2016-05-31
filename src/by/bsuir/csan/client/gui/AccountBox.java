package by.bsuir.csan.client.gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AccountBox {

    private static String login;
    private static String passHash;

    public static String getLogin() {
        return login;
    }

    public static String getPassHash() {
        return passHash;
    }

    public static void display(String title) {
        Stage accountStage = new Stage();
        accountStage.initModality(Modality.APPLICATION_MODAL);
        accountStage.setMinHeight(200);
        accountStage.setMinWidth(200);
        accountStage.setTitle("");
        login = "";
        passHash = "";

        TitledVBox accountBox = new TitledVBox(title);

        Region emptyVSpace = new Region();
        VBox.setVgrow(emptyVSpace, Priority.ALWAYS);
        Region emptyHSpace = new Region();
        HBox.setHgrow(emptyHSpace, Priority.ALWAYS);

        TextField loginTextField = new TextField();
        loginTextField.setPromptText("Login");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button okButton = new Button("OK");
        okButton.setMinWidth(80);
        okButton.setOnAction((e) -> {
            if (loginTextField.getText().equals("") || passwordField.getText().equals("")) {
                AlertBox.display("Fill empty fields!");
            } else {
                login = loginTextField.getText();
                passHash = Integer.toString(passwordField.getText().hashCode());
                accountStage.close();
            }
        });

        accountBox.addAll(loginTextField, passwordField, emptyVSpace, new HBox(15, emptyHSpace, okButton));
        accountStage.setScene(new Scene(accountBox));
        accountStage.showAndWait();
    }

}
