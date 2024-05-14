package ui;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.scene.canvas.*;

public class MapPageController {
    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ChoiceBox<ColorItem> colorChoiceBox;

    public class ColorItem {
        private String name;
        private Color color;
    
        public ColorItem(String name, Color color) {
            this.name = name;
            this.color = color;
        }
    
        @Override
        public String toString() {
            return name;
        }
    }

    public void initialize() {
        colorChoiceBox.getItems().addAll(
            new ColorItem("Sand", Color.web("#d9be5c")),
            new ColorItem("Green", Color.web("#48992f")),
            new ColorItem("Water", Color.web("#077ef5"))
        );

        if (drawingCanvas != null) {
            drawingCanvas.getGraphicsContext2D().setLineWidth(10);

            drawingCanvas.setOnMouseDragged(event -> {
                double x = event.getX();
                double y = event.getY();

                drawingCanvas.getGraphicsContext2D().setFill(colorChoiceBox.getValue().color);
                drawingCanvas.getGraphicsContext2D().fillOval(x, y, 20, 20);
            });
        } else {
            System.err.println("drawingCanvas is null");
        }
    }

    public void goBack() {
        Main.openGUI();
    }
}