package ui.controller;

import javafx.scene.control.Alert;
import java.util.HashMap;
import engine.parser.ExpressionParser;
import engine.solvers.MapHandler;
import engine.solvers.Utility;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import ui.Main;
import ui.helpers.CanvasToPng;
import ui.helpers.HeightMap3DChart;
import ui.screenFactory.ScreenInterface;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.WritableImage;
import javafx.scene.image.PixelReader;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class MapPageController implements ScreenInterface{
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

    @FXML
    private Text minHeightText;

    @FXML
    private Text maxHeightText;

    @FXML
    private ChoiceBox<String> disableWaterChoiceBox;

    private double[][] heightStorage;
    private int[][] initialGreen = new int[500][500];
    private double[] startBallPostion = new double[2];
    private double[] HolePostion = new double[2];
    private double radiusHole;
    private int mapSize;
    private String function;
    private double treeRadius;
    private double grassFrictionKINETIC;
    private double grassFrictionSTATIC;
    private boolean disableWater = false;

    GraphicsContext gc;

    private Parent root;

    
    public MapPageController() {
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    public void initializeParameters(String function, double xBall, double yBall, double xHole, double yHole, double radiusHole, double treeRadius, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        this.heightStorage = getHeightCoordinates(function);
        this.function = function;
        this.startBallPostion[0] = xBall;
        this.startBallPostion[1] = yBall;
        this.HolePostion[0] = xHole;
        this.HolePostion[1] = yHole;
        this.radiusHole = radiusHole;
        this.mapSize = 50;
        this.grassFrictionKINETIC = grassFrictionKINETIC;
        this.grassFrictionSTATIC = grassFrictionSTATIC;
        this.treeRadius = treeRadius;

        createScreen();
    }

    private void createScreen(){
        HeightMap3DChart chart = new HeightMap3DChart(heightStorage, chartPane);
        chart.display3DChart();
       
        renderInitialMap();
        drawBallAndHole();
    }


    public void initialize() {
        setUpChoiceBoxes();
        setUpCanvas();
    }

    private void setUpCanvas() {
        if (drawingCanvas != null) {
            this.gc = drawingCanvas.getGraphicsContext2D();
            gc.setLineWidth(30);

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
                        drawingCanvas.setOnMouseClicked(handler); // Enable clicking for other colors
                        drawingCanvas.setOnMouseDragged(handler); // Enable dragging for other colors
                    }
                }
            });

            // Make overlayCanvas transparent and disable mouse events
            overlayCanvas.setMouseTransparent(true);
        } else {
            System.err.println("drawingCanvas is null");
        }
    }

    private void setUpChoiceBoxes(){
        colorChoiceBox.getItems().addAll(
            new ColorItem("Sand", Color.rgb(160, 125, 0)),
            new ColorItem("Grass", Color.rgb(0, 125, 0)),
            new ColorItem("Water", Color.rgb(0, 125, 180)),
            new ColorItem("Tree", Color.rgb(120, 60, 35))
            // new ColorItem("Lift ground", Color.rgb(0, 125, 0)),
            // new ColorItem("Lower ground", Color.rgb(0, 125, 0))
        );
    
        mapSizeChoiceBox.getItems().addAll(5, 10, 25, 50);
        mapSizeChoiceBox.setValue(mapSize);
        mapSizeText.setText("Map size in meters: " + mapSize);
    
        disableWaterChoiceBox.getItems().addAll("Enable water", "Disable water");
        disableWaterChoiceBox.setValue("Enable water");
        disableWaterChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            disableWater = newValue.equals("Disable water");
            renderInitialMap();
            drawBallAndHole();
        });
    
        mapSizeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldSize, newSize) -> {
            if (newSize != null) {
                mapSize = newSize;
                mapSizeText.setText("Map size in meters: " + mapSize);
            }
        });
    }
    

    @FXML
    private void saveCanvasAndContinue() {
        CanvasToPng canvasToPng = new CanvasToPng(drawingCanvas);
        canvasToPng.saveCanvasAsPNG();

        MapHandler map = new MapHandler();
        String path = System.getProperty("user.dir") + "/src/main/resources/userInputMap.png";
        map.renderMap(this.initialGreen, path, HolePostion, radiusHole);

        Main mainInst = new Main();
        mainInst.setScreen("GAME", "", 0, 0, 0, 0, radiusHole, 0, grassFrictionKINETIC, grassFrictionSTATIC, startBallPostion, HolePostion);
    }

    @FXML
    private void changeMapSize() {
        int selectedMapSize = mapSizeChoiceBox.getValue();
        Utility.ratio = 500.0 / selectedMapSize;
        this.heightStorage = getHeightCoordinates(function);
        
        renderInitialMap();
        drawBallAndHole();
    }

    public void goBack() {
        Main mainInst = new Main();
        mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
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

    private double[] getMinMaxHeight() {
        double minHeight = Double.MAX_VALUE;
        double maxHeight = Double.MIN_VALUE;

        for (int i = 0; i < heightStorage.length; i++) {
            for (int j = 0; j < heightStorage[i].length; j++) {
                if (heightStorage[i][j] < minHeight) {
                    minHeight = heightStorage[i][j];
                }
                if (heightStorage[i][j] > maxHeight) {
                    maxHeight = heightStorage[i][j];
                }
            }
        }

        return new double[]{minHeight, maxHeight};
    }

    private Color getModifiedColor(double height) {
        if (height < MIN_HEIGHT || height > MAX_HEIGHT) {
            throw new Error("Out of range functions");
        } else {
            int gr = Utility.heightToColor(height);
            gr = Math.max(0, Math.min(255, gr));

            if (height < 0) {
                if (disableWater) {
                    return Color.rgb(0, gr, 0); // Green color for negative height when water is disabled
                } else {
                    return Color.rgb(0, gr, 150); // Blue color for negative height when water is enabled
                }
            } else {
                return Color.rgb(0, gr, 0);
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
    
        // Update min and max height texts
        double[] minMaxHeight = getMinMaxHeight();
        minHeightText.setText(String.format("Min height: %.2f", minMaxHeight[0]));
        maxHeightText.setText(String.format("Max height: %.2f", minMaxHeight[1]));
    
        // borders
        gc.setFill(Color.BLUE);
        gc.setLineWidth(2);
        gc.fillRect(0, 0, 500, 2);
        gc.fillRect(0, 498, 500, 2);
        gc.fillRect(0, 0, 2, 500);
        gc.fillRect(498, 0, 2, 500);
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
            if (colorChoiceBox.getValue().name.equals("Sand")) {
                baseColor=Color.rgb(160, Math.max(100, Math.min(255, initialGreen[x][y])),0);
            }
            if (colorChoiceBox.getValue().name.equals("Water")) {
                baseColor=Color.rgb(0, Math.min(255, initialGreen[x][y]),180);
            }
            gc.setFill(baseColor);
            double brushWidth = widthSlider.getValue();
            if (colorChoiceBox.getValue().color.equals(Color.rgb(120, 60, 35))) {
                gc.fillOval(x - 5, y - 5, 2*treeRadius*Utility.ratio, 2*treeRadius*Utility.ratio);
            } else {
                gc.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
            }
            // Update min and max height texts
            double[] minMaxHeight = getMinMaxHeight();
            minHeightText.setText(String.format("Min height: %.2f", minMaxHeight[0]));
            maxHeightText.setText(String.format("Max height: %.2f", minMaxHeight[1]));
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
        gc2.fillOval(holeX - radiusHole * Utility.ratio, holeY - radiusHole * Utility.ratio, 2*radiusHole * Utility.ratio, 2*radiusHole * Utility.ratio);
    }

    private void clearBallAndHole(GraphicsContext gc2) {
        gc2.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
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
}
