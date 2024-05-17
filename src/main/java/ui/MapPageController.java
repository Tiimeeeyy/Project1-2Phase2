package ui;

import java.util.HashMap;
import engine.parser.ExpressionParser;
import engine.solvers.MapHandler;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MapPageController {
    private static final double MAX_HEIGHT = 1.0;
    private static final double MIN_HEIGHT = -1.0;

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private Canvas overlayCanvas;

    @FXML
    private ChoiceBox<ColorItem> colorChoiceBox;

    @FXML
    private Slider widthSlider;

    @FXML
    private Pane chartPane;

    private double[][] heightStorage;
    private int[][] initialGreen = new int[500][500];
    private double[] startBallPostion = new double[2];
    private double[] HolePostion = new double[2];

    public MapPageController(String function, double xBall, double yBall, double xHole, double yHole) {
        this.heightStorage = getHeightCoordinates(function);
        startBallPostion[0] = xBall;
        startBallPostion[1] = yBall;
        HolePostion[0] = xHole;
        HolePostion[1] = yHole;
        System.out.println("Function: " + function);
        System.out.println("Ball position: " + xBall + ", " + yBall);
        System.out.println("Hole position: " + xHole + ", " + yHole);
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
                    double heightStep = 0.1;
                    updateHeightMap(ix, iy, heightStep, gc);
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

            // Make overlayCanvas transparent and disable mouse events
            overlayCanvas.setMouseTransparent(true);
        } else {
            System.err.println("drawingCanvas is null");
        }
        
        double[][] height = heightStorage;
        HeightMap3DChart chart3d = new HeightMap3DChart(height, chartPane);
        chart3d.display3DChart();

        drawBallAndHole();
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

            File file = new File(resourcesDir, "userInputMap.png");
            
            
            if (file.exists()) {
                if (!file.delete()) {
                    throw new IOException("Failed to delete existing file: " + file.getAbsolutePath());
                }
            }
            boolean imageWritten = ImageIO.write(bufferedImage, "png", file);
            if (!imageWritten) {
                throw new IOException("Failed to write image to file: " + file.getAbsolutePath());
            }

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
        MapHandler map = new MapHandler();
        String path = System.getProperty("user.dir") + "/src/main/resources/userInputMap.png";
        map.renderMap(this.initialGreen, path);
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
                currentCoordinates.put("x", (double) i / 10 - 25);
                currentCoordinates.put("y", (double) -j / 10 + 25);
                ExpressionParser parser = new ExpressionParser(func, currentCoordinates);
                heightStorage[i][j] = parser.evaluate();
            }
        }
        return heightStorage;
    }

    private Color getModifiedColor(Color baseColor, double height) {
        if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            throw new Error("Out of range functions");
        } else {
            double normalizedHeight = (height - MIN_HEIGHT) / (MAX_HEIGHT - MIN_HEIGHT);
            double brightnessFactor = 0.5 + normalizedHeight * 0.7;
            // Color color = baseColor.deriveColor(0, 1, brightnessFactor, 1);
            
            int gr=(int)Math.floor(75+normalizedHeight*100);
            Color color=Color.rgb(0, gr, 0);
            
            return color;
        }
    }

    private void renderInitialMap(GraphicsContext gc, Color baseColor) {
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 500; y++) {
                double height = heightStorage[x][y];
                Color heightColor = getModifiedColor(baseColor, height);
                this.initialGreen[x][y] = (int) Math.round(heightColor.getGreen() * 255);

                gc.getPixelWriter().setColor(x, y, heightColor);
            }
        }
        System.out.println("Initial map rendered with green color.");
    }

    private void updateHeightMap(int x, int y, double heightStep, GraphicsContext gc) {
        if (x >= 0 && x < 500 && y >= 0 && y < 500) {
            heightStorage[x][y] += heightStep;
            if (heightStorage[x][y] > MAX_HEIGHT) {
                heightStorage[x][y] = MAX_HEIGHT;
            } else if (heightStorage[x][y] < MIN_HEIGHT) {
                heightStorage[x][y] = MIN_HEIGHT;
            }
            double height = heightStorage[x][y];
            Color baseColor = colorChoiceBox.getValue().color;
            Color heightColor = getModifiedColor(baseColor, height);
            gc.setFill(heightColor);
            double brushWidth = widthSlider.getValue();

            if (colorChoiceBox.getValue().color.equals(Color.web("#654321"))) {
                gc.fillOval(x - 5, y - 5, 5, 5);
            } else {
                gc.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
            }
        } else {
            System.err.println("Mouse coordinates out of bounds: " + x + ", " + y);
        }
    }

    private void drawBallAndHole() {
        GraphicsContext gc = overlayCanvas.getGraphicsContext2D();
    
        // Центр холста
        double centerX = overlayCanvas.getWidth() / 2;
        double centerY = overlayCanvas.getHeight() / 2;
    
        double ballX = centerX + startBallPostion[0];
        double ballY = centerY - startBallPostion[1];  
        double holeX = centerX + HolePostion[0];
        double holeY = centerY - HolePostion[1];  
    
        gc.setFill(Color.WHITE);
        gc.fillOval(ballX - 0.5, ballY - 0.5, 1, 1);  // Мячик размером 1 пиксель
    
        gc.setFill(Color.BLACK);
        gc.fillOval(holeX - 1, holeY - 1, 2, 2);  // Лунка размером 2 пикселя
    }
}
