package by.bsuir.csan.client.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AlertBox {

    public static void display(String message) {
        Stage alertStage = new Stage();
        alertStage.initModality(Modality.APPLICATION_MODAL);
        alertStage.setTitle("Message");

        Label messageLabel = new Label(message);
        Button okButton = new Button("OK");
        okButton.setMinWidth(80);
        okButton.setOnAction((e) -> alertStage.close());

        VBox box = new VBox();
        box.setPadding(new Insets(15));
        box.setSpacing(15);
        box.setMinHeight(100);
        box.setMinWidth(200);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(messageLabel, okButton);

        alertStage.setScene(new Scene(box));
        alertStage.showAndWait();
    }
}
