package by.bsuir.csan.client.gui;

import javafx.scene.Node;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

public class TitledVBox extends TitledPane {

    private VBox vBox = new VBox();

    public TitledVBox(String title) {
        setText(title);
        setCollapsible(false);
        setPrefWidth(Region.USE_COMPUTED_SIZE);
        vBox.setSpacing(15);
        setContent(vBox);
    }

    public void addAll(Node... e) {
        vBox.getChildren().addAll(e);
    }
}
