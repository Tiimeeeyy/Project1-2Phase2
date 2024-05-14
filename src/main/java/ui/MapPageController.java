package ui;

import java.util.ArrayList;
import java.util.HashMap;
import engine.parser.ExpressionParser;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.paint.Color;
import javafx.scene.canvas.*;

public class MapPageController {
    @FXML
    private Canvas drawingCanvas;

    @FXML
    private ChoiceBox<ColorItem> colorChoiceBox;

    private String heightFunction;
    private double[][] heightStorage;

    public MapPageController(String function) {
        this.heightFunction = function;
        this.heightStorage = getHeightCoordinates(function);
    }

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
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.setLineWidth(10);

            renderInitialMap(gc, Color.web("#48992f"));

            drawingCanvas.setOnMouseDragged(event -> {
                double x = event.getX();
                double y = event.getY();
                System.out.println("Mouse dragged at: " + x + ", " + y);
                
                int ix = (int) x;
                int iy = (int) y;
                if (ix >= 0 && ix < 500 && iy >= 0 && iy < 500) {
                    double height = heightStorage[ix][iy];
                    Color baseColor = colorChoiceBox.getValue().color;
                    Color heightColor = getModifiedColor(baseColor, height);
                    
                    gc.setFill(heightColor);
                    gc.fillOval(x - 5, y - 5, 10, 10);
                }
            });
        } else {
            System.err.println("drawingCanvas is null");
        }
    }

    public void goBack() {
        Main.openGUI();
    }

    public static double[][] getHeightCoordinates(String func) {
        double[][] heightStorage = new double[500][500];
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                HashMap<String, Double> currentCoordinates = new HashMap<>();
                currentCoordinates.put("x", (double) i);
                currentCoordinates.put("y", (double) j);
                ExpressionParser parser = new ExpressionParser(func, currentCoordinates);
                heightStorage[i][j] = parser.evaluate();
            }
        }
        return heightStorage;
    }

    private void renderInitialMap(GraphicsContext gc, Color baseColor) {
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 500; y++) {
                double height = heightStorage[x][y];
                Color heightColor = getModifiedColor(baseColor, height);
                gc.getPixelWriter().setColor(x, y, heightColor);
            }
        }
        System.out.println("Initial map rendered with green color.");
    }

    private Color getModifiedColor(Color baseColor, double height) {
        double normalizedHeight = Math.min(Math.max(height / 200, 0), 1); 
        double brightnessFactor = 0.5 + normalizedHeight * 0.5; 
        Color color = baseColor.deriveColor(0, 1, brightnessFactor, 1);
        System.out.println("Height: " + height + ", Base Color: " + baseColor + ", Modified Color: " + color);
        return color;
    }
}
