package ui;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.canvas.*;

public class MapPageController {
    @FXML
    private ColorPicker ColorPicker;
    private Canvas Canvas;

    public void ColorPicker(ActionEvent e){
        GraphicsContext gc = Canvas.getGraphicsContext2D();
        gc.setFill(ColorPicker.getValue());
    }

}