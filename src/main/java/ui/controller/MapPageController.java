package ui.controller;

import java.util.HashMap;
import engine.parser.ExpressionParser;
import engine.solvers.MapHandler;
import engine.solvers.Utility;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import ui.Main;
import ui.helpers.CanvasToPng;
import ui.helpers.HeightMap3DChart;
import ui.screenFactory.ScreenInterface;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;


/**
 * Controller class for the map page.
 */
public class MapPageController implements ScreenInterface {
    private static final double MAX_HEIGHT = 10.0; // Maximum height for the height map
    private static final double MIN_HEIGHT = -10.0; // Minimum height for the height map
    private static double minCurrentHeight;
    private static double maxCurrentHeight;

    @FXML
    private Canvas drawingCanvas; // Canvas for drawing

    @FXML
    private Canvas overlayCanvas; // Canvas for overlay elements

    @FXML
    private ChoiceBox<ColorItem> colorChoiceBox; // Choice box for selecting colors

    @FXML
    private ChoiceBox<Integer> mapSizeChoiceBox; // Choice box for selecting map size

    @FXML
    private Slider widthSlider; // Slider for setting brush width

    @FXML
    private Pane chartPane; // Pane for displaying 3D chart

    @FXML
    private Text mapSizeText; // Text for displaying map size

    @FXML
    private Text minHeightText; // Text for displaying minimum height

    @FXML
    private Text maxHeightText; // Text for displaying maximum height

    @FXML
    private ChoiceBox<String> disableWaterChoiceBox; // Choice box for enabling/disabling water

    private double[][] heightStorage; // Storage for height values
    private int[][] initialGreen = new int[500][500]; // Initial green values
    private double[] startBallPostion = new double[2]; // Starting position of the ball
    private double[] HolePostion = new double[2]; // Position of the hole
    private double radiusHole; // Radius of the hole
    private int mapSize = 50; // Size of the map
    private String function; // Function for generating the height map
    private double treeRadius; // Radius of trees
    private double grassFrictionKINETIC; // Kinetic friction for grass
    private double grassFrictionSTATIC; // Static friction for grass
    private boolean disableWater = false; // Flag for disabling water

    GraphicsContext gc; // Graphics context for drawing

    private Parent root; // Root node

    /**
     * Constructor for the MapPageController.
     */
    public MapPageController() {
    }

    /**
     * Sets the root node.
     *
     * @param root the root node
     */
    public void setRoot(Parent root) {
        this.root = root;
    }

    /**
     * Gets the root node.
     *
     * @return the root node
     */
    @Override
    public Parent getRoot() {
        return root;
    }

    /**
     * Initializes parameters for the map.
     *
     * @param function              the function for generating the height map
     * @param xBall                 the x-coordinate of the ball's starting position
     * @param yBall                 the y-coordinate of the ball's starting position
     * @param xHole                 the x-coordinate of the hole's position
     * @param yHole                 the y-coordinate of the hole's position
     * @param radiusHole            the radius of the hole
     * @param treeRadius            the radius of trees
     * @param grassFrictionKINETIC  the kinetic friction for grass
     * @param grassFrictionSTATIC   the static friction for grass
     */
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

    /**
     * Creates and initializes the screen.
     */
    private void createScreen(){

       String newStr = "hello";
       newStr = newStr + "world";
        renderInitialMap();
        drawBallAndHole();
    }

    /**
     * Initializes the controller.
     */
    public void initialize() {
        setUpChoiceBoxes();
        setUpCanvas();
    }

    /**
     * Sets up the drawing canvas.
     */
    private void setUpCanvas() {
        if (drawingCanvas != null) {
            this.gc = drawingCanvas.getGraphicsContext2D();
            gc.setLineWidth(30);

            EventHandler<MouseEvent> handler = event -> {
                double x = event.getX();
                double y = event.getY();
                // System.out.println("Mouse event at: " + x + ", " + y);

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

    /**
     * Sets up choice boxes for the UI.
     */
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

    /**
     * Saves the canvas as PNG and proceeds to the game screen.
     */
    @FXML
    private void saveCanvasAndContinue() {
        CanvasToPng canvasToPng = new CanvasToPng(drawingCanvas);
        canvasToPng.saveCanvasAsPNG();

        MapHandler map = new MapHandler();
        String path = System.getProperty("user.dir") + "/src/main/resources/userInputMap.png";
        map.renderMap(this.initialGreen, path, HolePostion, radiusHole, true);

        Main mainInst = new Main();
        mainInst.setScreen("GAME", "", 0, 0, 0, 0, radiusHole, 0, grassFrictionKINETIC, grassFrictionSTATIC, startBallPostion, HolePostion);
    }

    /**
     * Changes the size of the map.
     */
    @FXML
    private void changeMapSize() {
        int selectedMapSize = mapSizeChoiceBox.getValue();
        Utility.ratio = 500.0 / selectedMapSize;
        this.heightStorage = getHeightCoordinates(function);

        renderInitialMap();
        drawBallAndHole();
    }

    /**
     * Navigates back to the input.
     */
    public void goBack() {
        Main mainInst = new Main();
        mainInst.setScreen("INPUT", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    /**
     * Generates height coordinates from the given function.
     *
     * @param func the function for generating the height map
     * @return a 2D array of height values
     */
    public static double[][] getHeightCoordinates(String func) {
        double[][] heightStorage = new double[500][500];

        for (int i = 0; i < 500; i++) {
            for (int j = 0; j < 500; j++) {
                HashMap<String, Double> currentCoordinates = new HashMap<>();
                currentCoordinates.put("x", Utility.pixelToCoordinate_X(i));
                currentCoordinates.put("y", Utility.pixelToCoordinate_Y(j));
                ExpressionParser parser = new ExpressionParser(func, currentCoordinates);
                double height = parser.evaluate();
                heightStorage[i][j] = height;

                // Update min and max height
                if (height < minCurrentHeight) {
                    minCurrentHeight = height;
                }
                if (height > maxCurrentHeight) {
                    maxCurrentHeight = height;
                }
            }
        }

        return heightStorage;
    }

    /**
     * Gets the modified color based on height.
     *
     * @param height the height value
     * @return the color corresponding to the height
     */
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

    private void create3DChart() {
        HeightMap3DChart heightMap3DChart = new HeightMap3DChart(heightStorage, chartPane);
        heightMap3DChart.display3DChart();
    }

    /**
     * Renders the initial map with height values.
     */
    private void renderInitialMap() {
        create3DChart();
        for (int x = 0; x < 500; x++) {
            for (int y = 0; y < 500; y++) {
                double height = heightStorage[x][y];
                Color heightColor = getModifiedColor(height);
                this.initialGreen[x][y] = (int) Math.round(heightColor.getGreen() * 255);
    
                this.gc.getPixelWriter().setColor(x, y, heightColor);
            }
        }    
        // Update min and max height texts
        minHeightText.setText(String.format("Min height: %.2f", minCurrentHeight));
        maxHeightText.setText(String.format("Max height: %.2f", maxCurrentHeight));
    
        // Draw borders
        gc.setFill(Color.BLUE);
        gc.setLineWidth(2);
        gc.fillRect(0, 0, 500, 2);
        gc.fillRect(0, 498, 500, 2);
        gc.fillRect(0, 0, 2, 500);
        gc.fillRect(498, 0, 2, 500);
    }

    /**
     * Updates the height map based on mouse interactions.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param heightStep the step value for height adjustment
     */
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
                baseColor = Color.rgb(160, Math.max(100, Math.min(255, initialGreen[x][y])), 0);
            }
            if (colorChoiceBox.getValue().name.equals("Water")) {
                baseColor = Color.rgb(0, Math.min(255, initialGreen[x][y]), 180);
            }
            gc.setFill(baseColor);
            double brushWidth = widthSlider.getValue();
            if (colorChoiceBox.getValue().color.equals(Color.rgb(120, 60, 35))) {
                gc.fillOval(x - 5, y - 5, 2 * treeRadius * Utility.ratio, 2 * treeRadius * Utility.ratio);
            } else {
                gc.fillOval(x - brushWidth / 2, y - brushWidth / 2, brushWidth, brushWidth);
            }
            // Update min and max height texts
            minHeightText.setText(String.format("Min height: %.2f", minCurrentHeight));
            maxHeightText.setText(String.format("Max height: %.2f", maxCurrentHeight));
        } else {
            System.err.println("Mouse coordinates out of bounds: " + x + ", " + y);
        }
    }

    /**
     * Draws the ball and the hole on the overlay canvas.
     */
    private void drawBallAndHole() {
        GraphicsContext gc2 = overlayCanvas.getGraphicsContext2D();

        clearBallAndHole(gc2);

        double ballX = Utility.coordinateToPixel_X(startBallPostion[0]);
        double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]);
        double holeX = Utility.coordinateToPixel_X(HolePostion[0]);
        double holeY = Utility.coordinateToPixel_Y(HolePostion[1]);

        gc2.setFill(Color.WHITE);
        gc2.fillOval(ballX - 0.5, ballY - 0.5, 0.1 * Utility.ratio, 0.1 * Utility.ratio);

        gc2.setFill(Color.BLACK);
        gc2.fillOval(holeX - radiusHole * Utility.ratio, holeY - radiusHole * Utility.ratio, 2 * radiusHole * Utility.ratio, 2 * radiusHole * Utility.ratio);
    }

    /**
     * Clears the ball and hole from the overlay canvas.
     *
     * @param gc2 the graphics context of the overlay canvas
     */
    private void clearBallAndHole(GraphicsContext gc2) {
        gc2.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
    }

    /**
     * Class representing a color item for the choice box.
     */
    public class ColorItem {
        private String name; // Name of the color item
        private Color color; // Color value

        /**
         * Constructor for ColorItem.
         *
         * @param name  the name of the color item
         * @param color the color value
         */
        public ColorItem(String name, Color color) {
            this.name = name;
            this.color = color;
        }

        /**
         * Returns the name of the color item.
         *
         * @return the name of the color item
         */
        @Override
        public String toString() {
            return name;
        }
    }
}
