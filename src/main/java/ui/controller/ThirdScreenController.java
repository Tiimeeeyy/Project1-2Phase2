package ui.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import ui.Main;
import ui.helpers.CircularSlider;
import ui.screenFactory.ScreenInterface;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Optional;

import engine.solvers.GolfGame;
import engine.solvers.Utility;
import engine.solvers.odeSolvers.RK4;

/**
 * Controller class for the third screen in the UI.
 */
public class ThirdScreenController implements ScreenInterface {

    @FXML
    private ImageView mapImageView; // ImageView to display the map

    @FXML
    private Button goBackButton; // Button to go back to the previous screen

    @FXML
    private Button hitButton; // Button to hit the ball

    @FXML
    private Slider powerSlider; // Slider to adjust the power of the hit

    @FXML
    private Pane circularSliderPane; // Pane to hold the circular slider

    @FXML
    private Canvas ballCanvas; // Canvas to draw the ball and trajectory

    @FXML
    private Label ballPositionLabel; // Label to display the ball position

    @FXML
    private Label shotCountLabel; // Label to display the shot count

    @FXML
    private Label directionLabel; // Label to display the direction of the hit

    @FXML
    private Label powerLabel; // Label to display the power of the hit

    @FXML
    private TextArea logTextArea; // TextArea to log events

    private CircularSlider circularSlider; // Custom circular slider for direction
    private double[] startBallPostion; // Starting position of the ball
    private double[] HolePostion; // Position of the hole
    private GolfGame golfGame; // Golf game engine
    private double grassFrictionKINETIC; // Kinetic friction on grass
    private double grassFrictionSTATIC; // Static friction on grass
    private int shotCount = 0; // Count of shots taken
    private ArrayList<double[]> fullTrajectory = new ArrayList<>(); // Full trajectory of the ball
    private boolean REACHED_THE_HOLE; // Flag to check if the ball reached the hole
    private Timeline timeline; // Animation timeline

    private Parent root; // Root node

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
     * Constructor for ThirdScreenController.
     */
    public ThirdScreenController() {
    }

    /**
     * Initializes parameters for the golf game.
     *
     * @param startBallPostion     the starting position of the ball
     * @param HolePostion          the position of the hole
     * @param radiusHole           the radius of the hole
     * @param grassFrictionKINETIC the kinetic friction on grass
     * @param grassFrictionSTATIC  the static friction on grass
     */
    public void initializeParameters(double[] startBallPostion, double[] HolePostion, double radiusHole, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        this.startBallPostion = startBallPostion;
        this.HolePostion = HolePostion;
        this.grassFrictionKINETIC = grassFrictionKINETIC;
        this.grassFrictionSTATIC = grassFrictionSTATIC;
        this.REACHED_THE_HOLE = false;
        double[] a = {grassFrictionKINETIC, grassFrictionSTATIC};
        this.golfGame = new GolfGame(new RK4(), a, 0.01, HolePostion, radiusHole, "src/main/resources/userInputMap.png");
        System.out.println("StartBallPostion: " + startBallPostion[0] + ", " + startBallPostion[1]);

        initialize();
    }

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        if (startBallPostion == null || HolePostion == null) {
            return; 
        }

        loadNewImage();

        circularSlider = new CircularSlider();
        circularSliderPane.getChildren().add(circularSlider);

        circularSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.stop();
                moveBallToEndOfTrajectory();
            }
            updateDirection(newVal);
            drawBallAndArrow();
        });
        powerSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (timeline != null && timeline.getStatus() == Timeline.Status.RUNNING) {
                timeline.stop();
                moveBallToEndOfTrajectory();
            }
            updatePower(newVal);
            drawBallAndArrow();
        });

        drawBallAndArrow();
        updateBallPositionLabel();
        updateShotCountLabel();
    }

    /**
     * Updates the direction label.
     *
     * @param newVal the new direction value
     */
    private void updateDirection(Number newVal) {
        double[] directionVector = circularSlider.getDirectionVector();
        directionLabel.setText(String.format("Direction: [%.2f, %.2f]", directionVector[0], directionVector[1]));
        System.out.println("Direction Vector: [" + directionVector[0] + ", " + directionVector[1] + "]");
    }

    /**
     * Updates the power label.
     *
     * @param newVal the new power value
     */
    private void updatePower(Number newVal) {
        powerLabel.setText(String.format("Power: %.2f", newVal.doubleValue()));
    }

    /**
     * Loads a new image for the map.
     */
    private void loadNewImage() {
        try {
            String userDir = System.getProperty("user.dir");
            File resourcesDir = new File(userDir, "src/main/resources");
            if (!resourcesDir.exists() || !resourcesDir.isDirectory()) {
                showAlert(Alert.AlertType.ERROR, "Load Failed", "The resources directory does not exist.");
                return;
            }

            File[] files = resourcesDir.listFiles();
            if (files == null) {
                showAlert(Alert.AlertType.ERROR, "Load Failed", "Failed to list files in the resources directory.");
                return;
            }

            for (File file : files) {
                if (file.isFile() && file.getName().equals("userInputMap.png")) {
                    String fileUrl = file.toURI().toURL().toExternalForm();
                    System.out.println("Loading image from: " + fileUrl);
                    Image image = new Image(new FileInputStream(file));

                    if (image.isError()) {
                        System.err.println("Error loading image: " + image.getException());
                        Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "Failed to load image: " + image.getException().getMessage()));
                        return;
                    }

                    mapImageView.setImage(image);
                    System.out.println("Image width: " + image.getWidth() + ", height: " + image.getHeight());
                    System.out.println("ImageView width: " + mapImageView.getFitWidth() + ", height: " + mapImageView.getFitHeight());

                    return;
                }
            }

            System.err.println("Image file userInputMap.png does not exist in the resources directory.");
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "The image file userInputMap.png does not exist in the resources directory."));

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "An error occurred while loading the image: " + e.getMessage()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Load Failed", "File not found: " + e.getMessage()));
        }
    }

    /**
     * Draws the trajectory of the ball.
     *
     * @param gc           the graphics context
     * @param currentIndex the current index in the trajectory
     */
    private void drawTrajectory(GraphicsContext gc, int currentIndex) {
        gc.setFill(javafx.scene.paint.Color.RED);

        for (int i = 0; i <= currentIndex; i++) {
            double[] point = fullTrajectory.get(i);
            double x = Utility.coordinateToPixel_X(point[0]);
            double y = Utility.coordinateToPixel_Y(point[1]);
            gc.fillOval(x, y, 1, 1);
        }
    }

    /**
     * Draws the ball and the arrow indicating direction and power.
     */
    private void drawBallAndArrow() {
        if (ballCanvas != null) {
            GraphicsContext gc = ballCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());

            double ballDiameter = 0.1 * Utility.ratio;
            double ballRadius = ballDiameter / 2.0;

            double ballX = Utility.coordinateToPixel_X(startBallPostion[0]) - ballRadius;
            double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]) - ballRadius;

            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);

            double[] directionVector = circularSlider.getDirectionVector();
            double arrowLength = powerSlider.getValue() * 5;
            double arrowX = ballX + ballRadius + directionVector[0] * arrowLength;
            double arrowY = ballY + ballRadius - directionVector[1] * arrowLength;

            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.setLineWidth(1);

            // Draw the arrow shaft
            gc.strokeLine(ballX + ballRadius, ballY + ballRadius, arrowX, arrowY);

            // Draw the arrowhead
            drawArrowhead(gc, arrowX, arrowY, directionVector);

            updateBallPositionLabel();
        } else {
            System.err.println("ballCanvas is null");
        }
    }

    /**
     * Draws the arrowhead.
     *
     * @param gc        the graphics context
     * @param x         the x-coordinate of the arrowhead
     * @param y         the y-coordinate of the arrowhead
     * @param direction the direction vector
     */
    private void drawArrowhead(GraphicsContext gc, double x, double y, double[] direction) {
        double arrowHeadSize = 5;
        double angle = Math.atan2(-direction[1], direction[0]);

        double x1 = x - arrowHeadSize * Math.cos(angle - Math.PI / 6);
        double y1 = y - arrowHeadSize * Math.sin(angle - Math.PI / 6);
        double x2 = x - arrowHeadSize * Math.cos(angle + Math.PI / 6);
        double y2 = y - arrowHeadSize * Math.sin(angle + Math.PI / 6);

        gc.setFill(javafx.scene.paint.Color.RED);
        gc.fillPolygon(new double[]{x, x1, x2}, new double[]{y, y1, y2}, 3);
    }

    /**
     * Handles the hit button action.
     */
    @FXML
    private void hit() {
        if (!REACHED_THE_HOLE) {
            // Clear the trajectory before each new hit
            fullTrajectory.clear();

            double[] directionVector = circularSlider.getDirectionVector();
            double power = powerSlider.getValue();
            System.out.println("Hit with power: " + power + ", direction: [" + directionVector[0] + ", " + directionVector[1] + "]");
            System.out.println("StartBallPostion: " + startBallPostion[0] + ", " + startBallPostion[1]);

            // Call the engine to calculate the trajectory
            double[] x = {startBallPostion[0], startBallPostion[1], power * directionVector[0], power * directionVector[1]};
            ArrayList<double[]> xpath = this.golfGame.shoot(x, true);

            // Update ball position and shot count
            if (xpath != null && !xpath.isEmpty()) {
                fullTrajectory.addAll(xpath);  // Add new trajectory points to the full trajectory
                double[] finalPosition = xpath.get(xpath.size() - 1);
                startBallPostion[0] = finalPosition[0];
                startBallPostion[1] = finalPosition[1];
                shotCount++;
            }

            String shotLog = String.format(
                "Shot %d: Hit to (%.2f, %.2f) with power %.2f.",
                shotCount, startBallPostion[0], startBallPostion[1], power);
            logEvent(shotLog);
            updateShotCountLabel();
            // Animate the ball movement along the trajectory
            animateBallMovement(fullTrajectory, power);

        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Goal!");
            alert.setHeaderText("Goal Reached, The ball has already reached the hole.");
            alert.setContentText("Would you like to go back or see the stats?");
            
            ButtonType backButton = new ButtonType("Back");
            ButtonType seeStatsButton = new ButtonType("See the stat");
            
            alert.getButtonTypes().setAll(backButton, seeStatsButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == backButton) {
                    goBack();
                } else if (result.get() == seeStatsButton) {
                    showStats();
                }
            }
        }
    }

    /**
     * Animates the ball movement along the trajectory.
     *
     * @param trajectory the trajectory of the ball
     * @param power      the power of the hit
     */
    private void animateBallMovement(ArrayList<double[]> trajectory, double power) {
        timeline = new Timeline();
        
        double duration = 5; 
        
        for (int i = 0; i < trajectory.size(); i++) {
            final int index = i;
            KeyFrame keyFrame = new KeyFrame(Duration.millis(duration * i), event -> {
                double[] point = trajectory.get(index);
                startBallPostion[0] = point[0];
                startBallPostion[1] = point[1];
                drawBallAndTrajectory(index);
                updateBallPositionLabel();
            });
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.setOnFinished(event -> {
            updateShotCountLabel();
            handlePostAnimation();
        });
        timeline.play();
    }

    /**
     * Handles post-animation events.
     */
    private void handlePostAnimation() {
        try {
            String message = golfGame.getMessage();
            if(golfGame.getTreeHit()){
                logEvent("** The ball hit a tree and bounced off **");
            }

            if (message.contains("Water")) {
                logEvent("!!--The ball landed in water--!!");
                showAlert(Alert.AlertType.INFORMATION, "Ball in Water", "The ball landed in water.");
            } else if (golfGame.isGoal()) {
                this.REACHED_THE_HOLE = true;
                logEvent("CONGRATULATIONS! The ball reached the hole.");
                showGoalAlert();
            } 
        } catch (Exception e) {
            // System.out.println("Error in message");
        }
    }

    /**
     * Moves the ball to the end of the trajectory.
     */
    private void moveBallToEndOfTrajectory() {
        if (!fullTrajectory.isEmpty()) {
            double[] finalPosition = fullTrajectory.get(fullTrajectory.size() - 1);
            startBallPostion[0] = finalPosition[0];
            startBallPostion[1] = finalPosition[1];
            drawFullTrajectory();
            updateBallPositionLabel();
        }
    }

    /**
     * Draws the ball and the trajectory.
     *
     * @param currentIndex the current index in the trajectory
     */
    private void drawBallAndTrajectory(int currentIndex) {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());
        drawTrajectory(gc, currentIndex);

        double ballDiameter = 0.1 * Utility.ratio;
        double ballRadius = ballDiameter / 2.0;

        double ballX = Utility.coordinateToPixel_X(startBallPostion[0]) - ballRadius;
        double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]) - ballRadius;

        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);
    }

    /**
     * Draws the full trajectory.
     */
    private void drawFullTrajectory() {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());
        drawTrajectory(gc, fullTrajectory.size() - 1);
        drawBallOnly();
    }

    /**
     * Draws the ball only.
     */
    private void drawBallOnly() {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        double ballDiameter = 0.1 * Utility.ratio;
        double ballRadius = ballDiameter / 2.0;

        double ballX = Utility.coordinateToPixel_X(startBallPostion[0]) - ballRadius;
        double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]) - ballRadius;

        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);
    }

    /**
     * Logs an event message.
     *
     * @param message the message to log
     */
    private void logEvent(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
        });
    }

    /**
     * Handles the go back button action.
     */
    @FXML
    private void goBack() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Retreat");
        alert.setHeaderText("Are you sure you want to go back?");
        alert.setContentText("All progress will be lost! \n\"Opportunities multiply as they are seized.\" – Sun Tzu, The Art of War");
        ButtonType backButton = new ButtonType("I have seen enough, take me back!");
        ButtonType stayButton = new ButtonType("Give me one more chance, captain!");

        
        alert.getButtonTypes().setAll(backButton, stayButton);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == backButton) {
                Main mainInst = new Main();
                mainInst.setScreen("INPUT", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
            } else if (result.get() == stayButton) {
                alert.close();
            }
        }
        
    }

    /**
     * Updates the ball position label.
     */
    private void updateBallPositionLabel() {
        ballPositionLabel.setText(String.format("Ball Position: (%.2f, %.2f)", startBallPostion[0], startBallPostion[1]));
    }

    /**
     * Updates the shot count label.
     */
    private void updateShotCountLabel() {
        shotCountLabel.setText("Shots: " + shotCount);
    }

    /**
     * Shows an alert with the given parameters.
     *
     * @param alertType the type of alert
     * @param title     the title of the alert
     * @param message   the message of the alert
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Shows a goal alert when the ball reaches the hole.
     */
    private void showGoalAlert() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Goal!");
            alert.setHeaderText("CONGRATULATIONS! The ball reached the hole.");
            alert.setContentText("Would you like to go back or see the stats?");
            
            ButtonType backButton = new ButtonType("Back");
            ButtonType seeStatsButton = new ButtonType("See the stat");
            
            alert.getButtonTypes().setAll(backButton, seeStatsButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent()) {
                if (result.get() == backButton) {
                    goBack();
                } else if (result.get() == seeStatsButton) {
                    showStats();
                }
            }
        });
    }

    /**
     * Shows the statistics of the game.
     */
    private void showStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Total Shots: ").append(shotCount).append("\n");
        stats.append("Game Log:\n");
        stats.append(logTextArea.getText());

        logEvent("Showing stats.");

        showAlert(Alert.AlertType.INFORMATION, "Stats:", stats.toString());
    }
}
