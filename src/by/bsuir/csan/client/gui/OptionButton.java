package by.bsuir.csan.client.gui;

import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;

public class OptionButton extends Button {

    private String trueStateText;
    private String falseStateText;
    private boolean state;

    private void updateText() {
        this.setText((state) ? trueStateText : falseStateText);
    }

    public OptionButton(String initialStateText, String alternativeStateText, boolean initialState) {

        if (initialState) {
            this.trueStateText = initialStateText;
            this.falseStateText = alternativeStateText;
        } else {
            this.trueStateText = alternativeStateText;
            this.falseStateText = initialStateText;
        }

        setState(initialState);
        updateText();
    }

    public void setState(boolean state) {
        this.state = state;
        updateText();
    }
    public void setOnActivation(EventHandler<? super MouseEvent> value) {
        onMouseClickedProperty().set(event -> {
            value.handle(event);
            state = !state;
            updateText();
        });
    }

    public boolean isActive() {
        return state;
    }

}
