package ui.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import engine.solvers.Utility;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import ui.Main;
import ui.screenFactory.ScreenInterface;

/**
 * Controller class for the first screen in the application.
 * It handles user input and transitions to the next screen based on the provided data.
 */
public class InputSecondController extends Parent implements ScreenInterface {

    @FXML
    private TextField X_BALL;
    @FXML
    private TextField Y_BALL;
    @FXML
    private TextField X_HOLE;
    @FXML
    private TextField Y_HOLE;
    @FXML
    private TextField RADIUS_HOLE;
    @FXML
    private TextField GRASS_FRICTION_KINETIC;
    @FXML
    private TextField GRASS_FRICTION_STATIC;

    @FXML
    private Canvas mapCanvas;

    private String relativePath;
    private Parent root;

    /**
     * Sets the root node for this controller.
     *
     * @param root the root node
     */
    public void setRoot(Parent root) {
        this.root = root;
    }

    /**
     * Returns the root node for this controller.
     *
     * @return the root node
     */
    @Override
    public Parent getRoot() {
        return root;
    }

    public void initialize(String relativePath) {
        this.relativePath = relativePath;
        drawMap();
    }

    private void drawMap() {
        try {
            File sourceFile = new File(relativePath);
            if (sourceFile.exists()) {
                Image mapImage = new Image(sourceFile.toURI().toString());
                GraphicsContext gc = mapCanvas.getGraphicsContext2D();
                gc.drawImage(mapImage, 0, 0, mapCanvas.getWidth(), mapCanvas.getHeight());
            } else {
                // System.out.println("Source file does not exist.");
            }
        } catch (Exception ex) {
            showAlert("Error!", "Failed to draw map", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void updateMapF() {
        drawMap();
        try {
            GraphicsContext gc = mapCanvas.getGraphicsContext2D();
            double xBall = Utility.coordinateToPixel_X(Double.parseDouble(X_BALL.getText()));
            double yBall = Utility.coordinateToPixel_Y(Double.parseDouble(Y_BALL.getText()));
            double xHole = Utility.coordinateToPixel_X(Double.parseDouble(X_HOLE.getText()));
            double yHole = Utility.coordinateToPixel_Y(Double.parseDouble(Y_HOLE.getText()));
            double radiusHole = Double.parseDouble(RADIUS_HOLE.getText());
           
            
            gc.setFill(Color.WHITE);
            gc.fillOval(xBall - 0.5, yBall - 0.5, 0.1 * Utility.ratio, 0.1 * Utility.ratio);

            gc.setFill(Color.BLACK);
            gc.fillOval(xHole - radiusHole * Utility.ratio, yHole - radiusHole * Utility.ratio, 2 * radiusHole * Utility.ratio, 2 * radiusHole * Utility.ratio);
        } catch (NumberFormatException e) {
            // Ignore 
        }
    }

    /**
     * Handles the action event to transition to the next screen.
     * It validates user inputs, deletes an existing file, and opens the next screen.
     *
     * @param event the action event
     */
    public void nextScreen(ActionEvent event) {
        double xBall, yBall, xHole, yHole, radiusHole, grassFrictionKINETIC, grassFrictionSTATIC;

        try {
            xBall = Double.parseDouble(X_BALL.getText());
            yBall = Double.parseDouble(Y_BALL.getText());
            xHole = Double.parseDouble(X_HOLE.getText());
            yHole = Double.parseDouble(Y_HOLE.getText());
            radiusHole = Double.parseDouble(RADIUS_HOLE.getText());
            grassFrictionKINETIC = Double.parseDouble(GRASS_FRICTION_KINETIC.getText());
            grassFrictionSTATIC = Double.parseDouble(GRASS_FRICTION_STATIC.getText());
        } catch (NumberFormatException e) {
            showAlert("Error!", "Invalid input, please check it and try again!", "Please enter valid numbers for the coordinates and radius.");
            return;
        }

        if (checkInput(grassFrictionKINETIC, grassFrictionSTATIC, radiusHole)) {
            deleteFile();
            createFile(xHole, yHole, radiusHole);
            openNextScreen(xBall, yBall, xHole, yHole, radiusHole, grassFrictionKINETIC, grassFrictionSTATIC);
        }
    }

    /**
     * Handles the action event to go back to the start screen.
     *
     * @param event the action event
     */
    @FXML
    private void goBack(ActionEvent event) {
        Main mainInst = new Main();
        mainInst.setScreen("LOAD", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    /**
     * Displays an alert dialog with the specified title, header, and content.
     *
     * @param title   the title of the alert
     * @param header  the header text of the alert
     * @param content the content text of the alert
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Opens the next screen with the specified parameters.
     *
     * @param xBall              the X coordinate of the ball
     * @param yBall              the Y coordinate of the ball
     * @param xHole              the X coordinate of the hole
     * @param yHole              the Y coordinate of the hole
     * @param radiusHole         the radius of the hole
     * @param grassFrictionKINETIC the kinetic friction of the grass
     * @param grassFrictionSTATIC  the static friction of the grass
     */
    private void openNextScreen(double xBall, double yBall, double xHole, double yHole, double radiusHole, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        try {
            Main mainInst = new Main();
            // System.out.println("grassFrictionKINETIC: " + grassFrictionKINETIC);
            // System.out.println("grassFrictionSTATIC: " + grassFrictionSTATIC);
            // System.out.println(relativePath);
            double[] startBallPostion = {xBall, yBall};
            double[] HolePostion = {xHole, yHole};
            mainInst.setScreen("GAME", "", 0, 0, 0, 0, radiusHole, 0, grassFrictionKINETIC, grassFrictionSTATIC, startBallPostion, HolePostion);

        } catch (Exception ex) {
            showAlert("Error!", "Failed to proceed to the next screen", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Creates a new file from the specified relative path and draws a hole on it.
     */
    private void createFile(double xHole, double yHole, double radius) {
        try {
            String userDir = System.getProperty("user.dir");
            File resourcesDir = new File(userDir, "src/main/resources");
            File newFile = new File(resourcesDir, "userInputMap.png");
            File sourceFile = new File(relativePath);

            if (sourceFile.exists()) {
                BufferedImage image = ImageIO.read(sourceFile);
                double[] hole = {xHole, yHole};
                drawHole(image, hole, radius);
                ImageIO.write(image, "png", newFile);
                // System.out.println("File created: " + newFile.getAbsolutePath());
            } else {
                // System.out.println("Source file does not exist.");
            }
        } catch (IOException ex) {
            showAlert("Error!", "Failed to create file", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Deletes an existing file if it exists.
     */
    private void deleteFile() {
        try {
            String userDir = System.getProperty("user.dir");
            File resourcesDir = new File(userDir, "src/main/resources");
            File file = new File(resourcesDir, "userInputMap.png");

            if (file.exists()) {
                System.gc();
                if (!file.delete()) {
                    throw new IOException("Failed to delete existing file: " + file.getAbsolutePath());
                } else {
                    // System.out.println("Existing file deleted: " + file.getAbsolutePath());
                }
            }
        } catch (IOException ex) {
            showAlert("Error!", "Failed to delete file", ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Validates the input values for grass friction and hole radius.
     *
     * @param grassFrictionKINETIC the kinetic friction of the grass
     * @param grassFrictionSTATIC  the static friction of the grass
     * @param radiusHole           the radius of the hole
     * @return true if all inputs are valid, false otherwise
     */
    private boolean checkInput(double grassFrictionKINETIC, double grassFrictionSTATIC, double radiusHole) {
        if (grassFrictionKINETIC < 0.05 || grassFrictionKINETIC > 0.1) {
            showAlert("Error!", "Invalid input for Grass Friction KINETIC", "Please enter a value between 0.05 and 0.1.");
            return false;
        } else if (grassFrictionSTATIC < 0.1 || grassFrictionSTATIC > 0.2) {
            showAlert("Error!", "Invalid input for Grass Friction STATIC", "Please enter a value between 0.1 and 0.2.");
            return false;
        } else if (radiusHole < 0.05 || radiusHole > 0.15) {
            showAlert("Error!", "Invalid input for Hole Radius", "Please enter a value between 0.05 and 0.15.");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Draws a hole on the map image.
     *
     * @param image     The map image.
     * @param hole      The coordinates of the hole.
     * @param radius    The radius of the hole.
     */
    private void drawHole(BufferedImage image, double[] hole, double radius) {
        int intR = (int) Math.floor(radius * Utility.ratio);
        int[] pixelHole = Utility.coordinateToPixel(hole);
        int centerX = pixelHole[0];
        int centerY = pixelHole[1];

        for (int x = -intR; x <= intR; x++) {
            for (int y = -intR; y <= intR; y++) {
                if (x * x + y * y <= intR * intR) {
                    int drawX = centerX + x;
                    int drawY = centerY + y;
                    if (drawX >= 0 && drawX < image.getWidth() && drawY >= 0 && drawY < image.getHeight()) {
                        image.setRGB(drawX, drawY, java.awt.Color.BLACK.getRGB());
                    }
                }
            }
        }
    }

    @FXML
    private void updateMap() {
        updateMapF();
    }
}
