package ui;

import java.util.ArrayList;
import java.util.HashMap;
import engine.parser.ExpressionParser;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import ui.MapPageController;


public class MapPageController {
    @FXML
    private Canvas drawingCanvas;

    @FXML
    private Canvas coordinatesCanvas;

    @FXML
    private ChoiceBox<ColorItem> colorChoiceBox;

    @FXML
    private Slider widthSlider;

    @FXML
    private Pane chartPane;

    private double[][] heightStorage;
    private double minHeight;
    private double maxHeight;

    public MapPageController(String function) {
        this.heightStorage = getHeightCoordinates(function);
        double[] heightRange = getHeightRange();
        this.minHeight = heightRange[0];
        this.maxHeight = heightRange[1];
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

    public void drawCoordinates(double x, double y) {
        System.out.println("Drawing coordinates: (" + x + ", " + y + ")");
        GraphicsContext gc = coordinatesCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, coordinatesCanvas.getWidth(), coordinatesCanvas.getHeight());
        gc.setFill(Color.RED); // Set the color for drawing the coordinates
        double circleSize = 100; // Set the size of the circle
        gc.fillOval(x - circleSize / 2, y - circleSize / 2, circleSize, circleSize); // Draw a larger circle at the coordinates
    }
    

    public void initialize() {
        colorChoiceBox.getItems().addAll(
            new ColorItem("Sand", Color.web("#d9be5c")),
            new ColorItem("Grass", Color.web("#48992f")),
            new ColorItem("Water", Color.web("#077ef5")),
            new ColorItem("Tree", Color.web("#654321"))

        );

        if (drawingCanvas != null) {
            GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
            gc.setLineWidth(30);

            renderInitialMap(gc, Color.web("#48992f"));

            EventHandler<MouseEvent> handler = event -> {
                double x = event.getX();
                double y = event.getY();
                System.out.println("Mouse event at: " + x + ", " + y);

                int ix = (int) x;
                int iy = (int) y;
                if (ix >= 0 && ix < 500 && iy >= 0 && iy < 500) {
                    double heightStep = (maxHeight - minHeight) / 100;
                    updateHeightMap(ix, iy, heightStep);
                    double height = heightStorage[ix][iy];
                    Color baseColor = colorChoiceBox.getValue().color;
                    Color heightColor = getModifiedColor(baseColor, height, minHeight, maxHeight);

                    gc.setFill(heightColor);
                    double brushWidth = widthSlider.getValue();
                    System.out.println("Color: " + colorChoiceBox.getValue().color + ", Brush width: " + brushWidth);
                    if (colorChoiceBox.getValue().color.equals(Color.web("#654321"))) {
                        gc.fillOval(x -  5, y - 5, 5, 5);
                    } else {
                        gc.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
                    }
                }
            };
            colorChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldColor, newColor) -> {
                if (newColor != null) {
                    if (newColor.color.equals(Color.web("#654321"))) {
                        drawingCanvas.setOnMouseClicked(handler);
                        drawingCanvas.setOnMouseDragged(null); // Disable dragging for dark brown
                    } else {
                        drawingCanvas.setOnMouseClicked(handler); // Disable clicking for other colors
                        drawingCanvas.setOnMouseDragged(handler);
                    }
                }
            });
            // drawingCanvas.setOnMouseDragged(handler);
            // drawingCanvas.setOnMouseClicked(handler);
        } else{
            System.err.println("drawingCanvas is null");

        }
        double[][] height = heightStorage;
        HeightMap3DChart chart3d = new HeightMap3DChart(height, chartPane);
        chart3d.display3DChart();

    }

    @FXML
    private void saveCanvasAsPNG() {
        try {
            WritableImage writableImage = new WritableImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight());
            drawingCanvas.snapshot(null, writableImage);

            BufferedImage bufferedImage = new BufferedImage((int) drawingCanvas.getWidth(), (int) drawingCanvas.getHeight(), BufferedImage.TYPE_INT_ARGB);
            PixelReader pixelReader = writableImage.getPixelReader();

            for (int y = 0; y < writableImage.getHeight(); y++) {
                for (int x = 0; x < writableImage.getWidth(); x++) {
                    int argb = pixelReader.getArgb(x, y);
                    bufferedImage.setRGB(x, y, argb);
                }
            }

            String userDir = System.getProperty("user.dir");
            File resourcesDir = new File(userDir, "src/main/resources");
            if (!resourcesDir.exists()) {
                showAlert(Alert.AlertType.ERROR, "Save Failed", "The directory you are trying to save to does not exist.");
                return;
            }

            File file = new File(resourcesDir, "newMap.png");
            boolean imageWritten = ImageIO.write(bufferedImage, "png", file);
            if (!imageWritten) {
                throw new IOException("Failed to write image to file: " + file.getAbsolutePath());
            }

            showAlert(Alert.AlertType.INFORMATION, "Save Successful", "Canvas has been saved as newMap.png in the resources folder.");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Save Failed", "An error occurred while saving the canvas: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void saveCanvasAndContinue() {
        saveCanvasAsPNG();
        Main.openThirdScreen();
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
        // System.out.println(heightStorage);
        return heightStorage;
    }

    private double[] getHeightRange() {
        double minHeight = Double.MAX_VALUE;
        double maxHeight = Double.MIN_VALUE;

        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                double height = heightStorage[i][j];
                if (height < minHeight) {
                    minHeight = height;
                }
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        }

        return new double[]{minHeight, maxHeight};
    }

    private void renderInitialMap(GraphicsContext gc, Color baseColor) {
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 500; y++) {
                double height = heightStorage[x][y];
                Color heightColor = getModifiedColor(baseColor, height, minHeight, maxHeight);
                gc.getPixelWriter().setColor(x, y, heightColor);
            }
        }
        System.out.println("Initial map rendered with green color.");
    }

    private void updateHeightMap(int x, int y, double heightStep) {
        if (x >= 0 && x < 500 && y >= 0 && y < 500) {
            heightStorage[x][y] += heightStep;
            // System.out.println("Height at (" + x + ", " + y + "): " + heightStorage[x][y]);
        } else {
            System.err.println("Mouse coordinates out of bounds: " + x + ", " + y);
        }
    }

    private Color getModifiedColor(Color baseColor, double height, double minHeight, double maxHeight) {
        double normalizedHeight = (height - minHeight) / (maxHeight - minHeight);
        double brightnessFactor = 0.6 + normalizedHeight * 0.7;
        Color color = baseColor.deriveColor(0, 1, brightnessFactor, 1);
        // System.out.println("Height: " + height + ", Base Color: " + baseColor + ", Modified Color: " + color);
        return color;
    }
}
