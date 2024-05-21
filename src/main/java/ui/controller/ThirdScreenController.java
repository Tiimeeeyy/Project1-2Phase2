package ui.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import ui.CircularSlider;
import ui.Main;
import javafx.application.Platform;
import javafx.fxml.FXML;
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
import engine.solvers.RK4;
import engine.solvers.Utility;

public class ThirdScreenController {

    @FXML
    private ImageView mapImageView;

    @FXML
    private Button goBackButton;

    @FXML
    private Button hitButton;

    @FXML
    private Slider powerSlider;

    @FXML
    private Pane circularSliderPane;

    @FXML
    private Canvas ballCanvas;

    @FXML
    private Label ballPositionLabel;

    @FXML
    private Label shotCountLabel;

    @FXML
    private Label directionLabel;

    @FXML
    private Label powerLabel;

    @FXML
    private TextArea logTextArea;

    private CircularSlider circularSlider;
    private double[] startBallPostion;
    private double[] HolePostion;
    private GolfGame golfGame;
    private double grassFrictionKINETIC;
    private double grassFrictionSTATIC;
    private int shotCount = 0;
    private ArrayList<double[]> fullTrajectory = new ArrayList<>();
    private boolean REACHED_THE_HOLE;
    private Timeline timeline;

    public ThirdScreenController(double[] startBallPostion, double[] HolePostion, double radiusHole, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        this.startBallPostion = startBallPostion;
        this.HolePostion = HolePostion;
        this.grassFrictionKINETIC = grassFrictionKINETIC;
        this.grassFrictionSTATIC = grassFrictionSTATIC;
        this.REACHED_THE_HOLE = false;
        // grass friction?
        double[] a = {grassFrictionKINETIC, grassFrictionSTATIC};
        this.golfGame = new GolfGame(new RK4(), a, 0.01, HolePostion, radiusHole, "src/main/resources/userInputMap.png");
        System.out.println("StartBallPostion: " + startBallPostion[0] + ", " + startBallPostion[1]);
    }

    @FXML
    public void initialize() {
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

    private void updateDirection(Number newVal) {
        double[] directionVector = circularSlider.getDirectionVector();
        directionLabel.setText(String.format("Direction: [%.2f, %.2f]", directionVector[0], directionVector[1]));
        System.out.println("Direction Vector: [" + directionVector[0] + ", " + directionVector[1] + "]");
    }

    private void updatePower(Number newVal) {
        powerLabel.setText(String.format("Power: %.2f", newVal.doubleValue()));
    }

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

    private void drawTrajectory(GraphicsContext gc, int currentIndex) {
        gc.setFill(javafx.scene.paint.Color.RED);

        for (int i = 0; i <= currentIndex; i++) {
            double[] point = fullTrajectory.get(i);
            double x = Utility.coordinateToPixel_X(point[0]);
            double y = Utility.coordinateToPixel_Y(point[1]);
            gc.fillOval(x, y, 1, 1);
        }
    }

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

    private void moveBallToEndOfTrajectory() {
        if (!fullTrajectory.isEmpty()) {
            double[] finalPosition = fullTrajectory.get(fullTrajectory.size() - 1);
            startBallPostion[0] = finalPosition[0];
            startBallPostion[1] = finalPosition[1];
            drawFullTrajectory();
            updateBallPositionLabel();
        }
    }

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

    private void drawFullTrajectory() {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());
        drawTrajectory(gc, fullTrajectory.size() - 1);
        drawBallOnly();
    }

    private void drawBallOnly() {
        GraphicsContext gc = ballCanvas.getGraphicsContext2D();
        double ballDiameter = 0.1 * Utility.ratio;
        double ballRadius = ballDiameter / 2.0;

        double ballX = Utility.coordinateToPixel_X(startBallPostion[0]) - ballRadius;
        double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]) - ballRadius;

        gc.setFill(javafx.scene.paint.Color.WHITE);
        gc.fillOval(ballX, ballY, ballDiameter, ballDiameter);
    }

    private void logEvent(String message) {
        Platform.runLater(() -> {
            logTextArea.appendText(message + "\n");
        });
    }

    @FXML
    private void goBack() {
        Main.openGUI();
    }

    private void updateBallPositionLabel() {
        ballPositionLabel.setText(String.format("Ball Position: (%.2f, %.2f)", startBallPostion[0], startBallPostion[1]));
    }

    private void updateShotCountLabel() {
        shotCountLabel.setText("Shots: " + shotCount);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

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

    private void showStats() {
        StringBuilder stats = new StringBuilder();
        stats.append("Total Shots: ").append(shotCount).append("\n");
        stats.append("Game Log:\n");
        stats.append(logTextArea.getText());

        logEvent("Showing stats.");

        showAlert(Alert.AlertType.INFORMATION, "Stats:", stats.toString());
    }
}
