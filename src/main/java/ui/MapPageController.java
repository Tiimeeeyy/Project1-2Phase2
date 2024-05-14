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
    private ArrayList<Double> heightStorage = new ArrayList<Double>();


    public MapPageController(String function){
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

    public static ArrayList<Double> getHeightCoordinates(String func){
        ArrayList<Double> heightStorage = new ArrayList<Double>();
       for(double i = 0;i<500;i++){
           for(double j = 0;j<500;j++){
               HashMap<String, Double> currentCoordinates = new HashMap<>();
               currentCoordinates.put("x", i);
               currentCoordinates.put("y", j);
               ExpressionParser parser = new ExpressionParser(func, currentCoordinates);
               heightStorage.add(parser.evaluate());
           }
       }
       return heightStorage;
   }    
}