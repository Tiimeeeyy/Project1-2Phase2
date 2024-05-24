package ui.controller;

import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import ui.Main;
import ui.screenFactory.ScreenInterface;

/**
 * Controller class for the first screen in the application.
 * It handles user input and transitions to the next screen based on the provided data.
 */
public class FirstScreenController extends Parent implements ScreenInterface {

    @FXML
    private TextField FunctionTextfield;
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
    private TextField TREE_RADIUS;
    @FXML
    private TextField GRASS_FRICTION_KINETIC;
    @FXML
    private TextField GRASS_FRICTION_STATIC;

    private String function;
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

    /**
     * Handles the action event to transition to the next screen.
     * It validates user inputs, deletes an existing file, and opens the next screen.
     *
     * @param event the action event
     */
    public void nextScreen(ActionEvent event) {
        function = FunctionTextfield.getText();
        double xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC;

        try {
            xBall = Double.parseDouble(X_BALL.getText());
            yBall = Double.parseDouble(Y_BALL.getText());
            xHole = Double.parseDouble(X_HOLE.getText());
            yHole = Double.parseDouble(Y_HOLE.getText());
            radiusHole = Double.parseDouble(RADIUS_HOLE.getText());
            treeRadius = Double.parseDouble(TREE_RADIUS.getText());
            grassFrictionKINETIC = Double.parseDouble(GRASS_FRICTION_KINETIC.getText());
            grassFrictionSTATIC = Double.parseDouble(GRASS_FRICTION_STATIC.getText());
        } catch (NumberFormatException e) {
            showAlert("Error!", "Invalid input, please check it and try again!", "Please enter valid numbers for the coordinates and radius.");
            return;
        }

        if (checkInput(grassFrictionKINETIC, grassFrictionSTATIC, treeRadius, radiusHole)) {
            deleteFile();
            openNextScreen(xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC);
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
        mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
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
     * @param treeRadius         the radius of the tree
     * @param grassFrictionKINETIC the kinetic friction of the grass
     * @param grassFrictionSTATIC  the static friction of the grass
     */
    private void openNextScreen(double xBall, double yBall, double xHole, double yHole, double radiusHole, double treeRadius, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        try {
            Main mainInst = new Main();
            mainInst.setScreen("MAP", function, xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC, null, null);
        } catch (Exception ex) {
            showAlert("Error!", "Failed to proceed to the next screen", ex.getMessage());
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
                    System.out.println("Existing file deleted: " + file.getAbsolutePath());
                }
            }
        } catch (IOException ex) {
            showAlert("Error!", "Failed to delete file", ex.getMessage());
            ex.printStackTrace();
            return;
        }
    }

    /**
     * Validates the input values for grass friction, tree radius, and hole radius.
     *
     * @param grassFrictionKINETIC the kinetic friction of the grass
     * @param grassFrictionSTATIC  the static friction of the grass
     * @param treeRadius           the radius of the tree
     * @param radiusHole           the radius of the hole
     * @return true if all inputs are valid, false otherwise
     */
    private boolean checkInput(double grassFrictionKINETIC, double grassFrictionSTATIC, double treeRadius, double radiusHole) {
        if (function.isEmpty()) {
            showAlert("Error!", "Invalid input for function", "Please enter a valid function.");
            return false;
        } else if (grassFrictionKINETIC < 0.05 || grassFrictionKINETIC > 0.1) {
            showAlert("Error!", "Invalid input for Grass Friction KINETIC", "Please enter a value between 0.05 and 0.1.");
            return false;
        } else if (grassFrictionSTATIC < 0.1 || grassFrictionSTATIC > 0.2) {
            showAlert("Error!", "Invalid input for Grass Friction STATIC", "Please enter a value between 0.1 and 0.2.");
            return false;
        } else if (treeRadius < 0.05 || treeRadius > 0.5) {
            showAlert("Error!", "Invalid input for Tree Radius", "Please enter a value between 0.05 and 0.15.");
            return false;
        } else if (radiusHole < 0.05 || radiusHole > 0.15) {
            showAlert("Error!", "Invalid input for Hole Radius", "Please enter a value between 0.05 and 0.1.");
            return false;
        } else {
            return true;
        }
    }
}
