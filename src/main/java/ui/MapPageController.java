package ui;

import java.util.HashMap;
import engine.parser.ExpressionParser;
import engine.solvers.MapHandler;
import engine.solvers.Utility;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
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
    private static final double MAX_HEIGHT = 10.0;
    private static final double MIN_HEIGHT = -10.0;

    @FXML
    private Canvas drawingCanvas;

    @FXML
    private Canvas overlayCanvas;

    @FXML
    private ChoiceBox<ColorItem> colorChoiceBox;

    @FXML
    private ChoiceBox<Integer> mapSizeChoiceBox;

    @FXML
    private Slider widthSlider;

    @FXML
    private Pane chartPane;

    @FXML
    private Text mapSizeText;

    private double[][] heightStorage;
    private int[][] initialGreen = new int[500][500];
    private double[] startBallPostion = new double[2];
    private double[] HolePostion = new double[2];
    private double radiusHole;
    private int mapSize;
    private String function;
    private double treeRadius;
    private double grassFriction;

    GraphicsContext gc;

    public MapPageController(String function, double xBall, double yBall, double xHole, double yHole, double radiusHole, double treeRadius, double grassFriction) {
        this.heightStorage = getHeightCoordinates(function);
        this.function = function;
        startBallPostion[0] = xBall;
        startBallPostion[1] = yBall;
        HolePostion[0] = xHole;
        HolePostion[1] = yHole;
        this.radiusHole = radiusHole;
        this.mapSize = 50;

        this.grassFriction = grassFriction;
        this.treeRadius = treeRadius;

        System.out.println("Function: " + function);
        System.out.println("Ball position: " + xBall + ", " + yBall);
        System.out.println("Hole position: " + xHole + ", " + yHole);
        System.out.println("Hole Radius: " + radiusHole);
        System.out.println("Map Size: " + mapSize);
        System.out.println("Map ratio: " + Utility.ratio);

        System.out.println("Tree Radius: " + treeRadius);
        System.out.println("Grass Friction: " + grassFriction);
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
            new ColorItem("Sand", Color.rgb(160, 125, 0)),
            new ColorItem("Grass", Color.rgb(0, 125, 0)),
            new ColorItem("Water", Color.rgb(0, 125, 180)),
            new ColorItem("Tree", Color.rgb(120, 60, 35)),
            new ColorItem("Lift ground", Color.rgb(0, 125, 0)),
            new ColorItem("Lower ground", Color.rgb(0, 125, 0))
        );

        mapSizeChoiceBox.getItems().addAll(5, 10, 25, 50);
        mapSizeChoiceBox.setValue(mapSize);
        mapSizeText.setText("Map size in meters: " + mapSize);

        mapSizeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSize, newSize) -> {
            if (newSize != null) {
                mapSize = newSize;
                mapSizeText.setText("Map size in meters: " + mapSize);
            }
        });

        if (drawingCanvas != null) {
            this.gc = drawingCanvas.getGraphicsContext2D();
            gc.setLineWidth(30);

            renderInitialMap();

            EventHandler<MouseEvent> handler = event -> {
                double x = event.getX();
                double y = event.getY();
                System.out.println("Mouse event at: " + x + ", " + y);

                int ix = (int) x;
                int iy = (int) y;
                if (ix >= 0 && ix < 500 && iy >= 0 && iy < 500) {
                    double heightStep = 0.1;
                    updateHeightMap(ix, iy, heightStep);
                }
            };

            colorChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldColor, newColor) -> {
                if (newColor != null) {
                    if (newColor.color.equals(Color.rgb(120, 60, 35))) {
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
    
            if (mapSize == 50) {
                GraphicsContext gc = drawingCanvas.getGraphicsContext2D();
                gc.setFill( Color.rgb(0, 0, 150));
                // 12???
                gc.setLineWidth(12);

                gc.fillRect(0, 0, 500, 2);
                gc.fillRect(0, 498, 500, 2);
                gc.fillRect(0, 0, 2, 500);
                gc.fillRect(498, 0, 2, 500);
    
                drawingCanvas.snapshot(null, writableImage);
                pixelReader = writableImage.getPixelReader();
                for (int y = 0; y < writableImage.getHeight(); y++) {
                    for (int x = 0; x < writableImage.getWidth(); x++) {
                        int argb = pixelReader.getArgb(x, y);
                        bufferedImage.setRGB(x, y, argb);
                    }
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
        map.renderMap(this.initialGreen, path, HolePostion, radiusHole);
        Main.openThirdScreen(startBallPostion, HolePostion, radiusHole);
    }

    @FXML
    private void changeMapSize() {
        int selectedMapSize = mapSizeChoiceBox.getValue();
        Utility.ratio = 500.0 / selectedMapSize;
        this.heightStorage = getHeightCoordinates(function);
        renderInitialMap();
        drawBallAndHole();

        System.out.println("Selected map size: " + selectedMapSize + " meters");
    }

    public void goBack() {
        Main.openGUI();
    }

    public static double[][] getHeightCoordinates(String func) {
        double[][] heightStorage = new double[500][500];
        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                HashMap<String, Double> currentCoordinates = new HashMap<>();
                currentCoordinates.put("x", Utility.pixelToCoordinate_X(i));
                currentCoordinates.put("y", Utility.pixelToCoordinate_Y(j));
                ExpressionParser parser = new ExpressionParser(func, currentCoordinates);
                heightStorage[i][j] = parser.evaluate();
            }
        }
        return heightStorage;
    }

    private Color getModifiedColor(double height) {
        if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            throw new Error("Out of range functions");
        } else {
            int gr = Utility.heightToColor(height);
            gr = Math.max(0, Math.min(255, gr));

            if (height < 0) {
                Color color = Color.rgb(0, gr, 150);
                return color;
            } else {
                Color color = Color.rgb(0, gr, 0);
                return color;
            }
        }
    }

    private void renderInitialMap() {
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 500; y++) {
                double height = heightStorage[x][y];
                Color heightColor = getModifiedColor(height);
                this.initialGreen[x][y] = (int) Math.round(heightColor.getGreen() * 255);
    
                this.gc.getPixelWriter().setColor(x, y, heightColor);
            }
        }
        System.out.println("Initial map rendered with green color.");
    
        if (mapSize == 50) {
            gc.setFill(Color.BLUE);
            gc.setLineWidth(2);
            gc.fillRect(0, 0, 500, 2);
            gc.fillRect(0, 498, 500, 2);
            gc.fillRect(0, 0, 2, 500);
            gc.fillRect(498, 0, 2, 500);
        }
    }

    private void updateHeightMap(int x, int y, double heightStep) {
        if (x >= 0 && x < 500 && y >= 0 && y < 500) {
            heightStorage[x][y] += heightStep;
            if (heightStorage[x][y] > MAX_HEIGHT) {
                heightStorage[x][y] = MAX_HEIGHT;
            } else if (heightStorage[x][y] < MIN_HEIGHT) {
                heightStorage[x][y] = MIN_HEIGHT;
            }
            Color baseColor = Color.rgb(0, Math.min(255, initialGreen[x][y]), 0);
            if (colorChoiceBox.getValue().name.equals("Tree")) {
                baseColor = Color.rgb(120, 60, 35);
            } 
            // else {
            //     baseColor = Color.rgb(
            //         (int) (colorChoiceBox.getValue().color.getRed() * 255),
            //         Math.min(255, initialGreen[x][y]),
            //         (int) (colorChoiceBox.getValue().color.getBlue() * 255)
            //     );
            // }
            if (colorChoiceBox.getValue().name.equals("Sand")) {
                baseColor=Color.rgb(160, Math.max(100, Math.min(255, initialGreen[x][y])),0);
            }
            if (colorChoiceBox.getValue().name.equals("Water")) {
                baseColor=Color.rgb(0, Math.min(255, initialGreen[x][y]),180);
            }
            gc.setFill(baseColor);
            double brushWidth = widthSlider.getValue();
            if (colorChoiceBox.getValue().color.equals(Color.rgb(120, 60, 35))) {
                gc.fillOval(x - 5, y - 5, 5, 5);
            } else {
                gc.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
            }
        } else {
            System.err.println("Mouse coordinates out of bounds: " + x + ", " + y);
        }
    }

    private void drawBallAndHole() {
        GraphicsContext gc2 = overlayCanvas.getGraphicsContext2D();

        clearBallAndHole(gc2);

        double ballX = Utility.coordinateToPixel_X(startBallPostion[0]);
        double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]);
        double holeX = Utility.coordinateToPixel_X(HolePostion[0]);
        double holeY = Utility.coordinateToPixel_Y(HolePostion[1]);

        gc2.setFill(Color.WHITE);
        gc2.fillOval(ballX - 0.5, ballY - 0.5, 0.1* Utility.ratio, 0.1* Utility.ratio);

        gc2.setFill(Color.BLACK);
        gc2.fillOval(holeX - 1, holeY - 1, radiusHole * Utility.ratio, radiusHole * Utility.ratio);
    }

    private void clearBallAndHole(GraphicsContext gc2) {
        gc2.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
    }
}
