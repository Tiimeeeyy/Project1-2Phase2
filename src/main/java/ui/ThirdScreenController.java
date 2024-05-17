package ui;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

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

    private CircularSlider circularSlider;
    private double[] startBallPostion;
    private double[] HolePostion;

    public ThirdScreenController(double[] startBallPostion, double[] HolePostion, double radiusHole) {
        this.startBallPostion = startBallPostion;
        this.HolePostion = HolePostion;
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
        powerSlider.valueProperty().addListener((obs, oldVal, newVal) -> drawBallAndArrow());

        drawBallAndArrow();
    }

    private void updateDirection(Number newVal) {
        double[] directionVector = circularSlider.getDirectionVector();
        System.out.println("Direction Vector: [" + directionVector[0] + ", " + directionVector[1] + "]");
    }

    private void updatePower(Number newVal) {
        System.out.println("Power: " + newVal.doubleValue());
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

            double centerX = ballCanvas.getWidth() / 2;
            double centerY = ballCanvas.getHeight() / 2;

            double ballRadius = 1; 
            double ballX = centerX + startBallPostion[0];
            double ballY = centerY - startBallPostion[1];  

            gc.setFill(javafx.scene.paint.Color.WHITE);
            gc.fillOval(ballX - ballRadius / 2, ballY - ballRadius / 2, ballRadius, ballRadius);

            double[] directionVector = circularSlider.getDirectionVector();
            double arrowLength = powerSlider.getValue() * 5;
            double arrowX = ballX + directionVector[0] * arrowLength;
            double arrowY = ballY - directionVector[1] * arrowLength;

            gc.setStroke(javafx.scene.paint.Color.RED);
            gc.setLineWidth(1);
            gc.strokeLine(ballX, ballY, arrowX, arrowY);
        } else {
            System.err.println("ballCanvas is null");
        }
    }

    @FXML
    private void hit() {
        double[] directionVector = circularSlider.getDirectionVector();
        double power = powerSlider.getValue();
        System.out.println("Hit with power: " + power + ", direction: [" + directionVector[0] + ", " + directionVector[1] + "]");
        System.out.println("StartBallPostion: " + startBallPostion[0] + ", " + startBallPostion[1]);

        // call the engine to calculate the trajectory

        // drawBallAndArrow();
    }

    @FXML
    private void goBack() {
        Main.openGUI();
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
