package by.bsuir.csan.client.gui;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class TitledHBox extends TitledPane {

    private HBox hBox = new HBox();

    public TitledHBox(String title) {
        setText(title);
        setCollapsible(false);
        setPrefWidth(Region.USE_COMPUTED_SIZE);
        hBox.setSpacing(15);
        setContent(hBox);
    }

    public void add(Node e) {
        hBox.getChildren().add(e);
    }

    public void addAll(Node... e) {
        hBox.getChildren().addAll(e);
    }
}
