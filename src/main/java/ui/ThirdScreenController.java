package ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.util.ArrayList;

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

    private CircularSlider circularSlider;
    private double[] startBallPostion;
    private double[] HolePostion;
    private GolfGame golfGame;
    private int shotCount = 0;

    public ThirdScreenController(double[] startBallPostion, double[] HolePostion, double radiusHole) {
        this.startBallPostion = startBallPostion;
        this.HolePostion = HolePostion;
        double[] a={0.05,0.12};
        this.golfGame=new GolfGame(new RK4(), a, 0.01, HolePostion, radiusHole, "src/main/resources/userInputMap.png");
        System.out.println("StartBallPostion: " + startBallPostion[0] + ", " + startBallPostion[1]);
    }

    @FXML
    public void initialize() {
        loadNewImage();
    
        circularSlider = new CircularSlider();
        circularSliderPane.getChildren().add(circularSlider);
    
        circularSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateDirection(newVal);
            drawBallAndArrow();
        });
        powerSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
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

    private void drawBallAndArrow() {
        if (ballCanvas != null) {
            GraphicsContext gc = ballCanvas.getGraphicsContext2D();
            gc.clearRect(0, 0, ballCanvas.getWidth(), ballCanvas.getHeight());
    
            double ballX = Utility.coordinateToPixel_X(startBallPostion[0]);
            double ballY = Utility.coordinateToPixel_Y(startBallPostion[1]);
    
            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(ballX - 5, ballY - 5, 10, 10);
    
            double[] directionVector = circularSlider.getDirectionVector();
            double arrowLength = powerSlider.getValue() * 5;
            double arrowX = ballX + directionVector[0] * arrowLength;
            double arrowY = ballY - directionVector[1] * arrowLength; 
    
            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.setLineWidth(2);
    
            // Draw the arrow shaft
            gc.strokeLine(ballX, ballY, arrowX, arrowY);
    
            // Draw the arrowhead
            drawArrowhead(gc, arrowX, arrowY, directionVector);
    
            updateBallPositionLabel();
        } else {
            System.err.println("ballCanvas is null");
        }
    }
    
    private void drawArrowhead(GraphicsContext gc, double x, double y, double[] direction) {
        double arrowHeadSize = 10;
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
        double[] directionVector = circularSlider.getDirectionVector();
        double power = powerSlider.getValue();
        System.out.println("Hit with power: " + power + ", direction: [" + directionVector[0] + ", " + directionVector[1] + "]");
        System.out.println("StartBallPostion: " + startBallPostion[0] + ", " + startBallPostion[1]);

        // call the engine to calculate the trajectory
        double[] x = {startBallPostion[0], startBallPostion[1], power * directionVector[0], power * directionVector[1]};
        ArrayList<double[]> xpath = this.golfGame.shoot(x, true);

        // Update ball position and shot count
        if (xpath != null && !xpath.isEmpty()) {
            double[] finalPosition = xpath.get(xpath.size() - 1);
            startBallPostion[0] = finalPosition[0];
            startBallPostion[1] = finalPosition[1];
            shotCount++;
        }

        drawBallAndArrow();
        updateShotCountLabel();
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
}
