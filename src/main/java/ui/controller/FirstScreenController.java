package ui.controller;

import java.io.File;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ui.Main;

public class FirstScreenController {
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

    @FXML
    public void initialize() {

    }

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
            showAlert("Error!", "Invalid input for coordinates or radius", "Please enter valid numbers for the coordinates and radius.");
            return;
        }

        System.out.println("Tree Radius: " + treeRadius);
        System.out.println("Grass Friction KINETIC: " + grassFrictionKINETIC);
        System.out.println("Grass Friction STATIC: " + grassFrictionKINETIC);

        if (function.isEmpty()) {
            showAlert("Error!", "Invalid input for function", "Please enter a valid function.");
            return;
        }
        if(grassFrictionKINETIC>=0.05 && grassFrictionKINETIC<=0.1){
            System.out.println("Grass Friction KINETIC is valid");
        }else{
            showAlert("Error!", "Invalid input for Grass Friction KINETIC", "Please enter a value between 0.05 and 0.1.");
            return;
        }

        if (grassFrictionSTATIC>=0.1 && grassFrictionSTATIC<=0.2){
            System.out.println("Grass Friction STATIC is valid");
        }else{
            showAlert("Error!", "Invalid input for Grass Friction STATIC", "Please enter a value between 0.1 and 0.2.");
            return;
        }

        if (treeRadius>=0.05 && treeRadius<=0.15){
            System.out.println("Tree Radius is valid");
        }else{  
            showAlert("Error!", "Invalid input for Tree Radius", "Please enter a value between 0.05 and 0.15.");
            return;
        }

        if (radiusHole>=0.05 && radiusHole<=0.15){
            System.out.println("Hole Radius is valid");
        }else{  
            showAlert("Error!", "Invalid input for Hole Radius", "Please enter a value between 0.05 and 0.1.");
            return;
        }

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

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/design/MapPage.fxml"));
            MapPageController controller = new MapPageController(function, xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC);
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Map Creating Screen");
            stage.show();
        } catch (IOException ex) {
            showAlert("Error!", "Failed to proceed to the next screen", ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML
    private void goBack(ActionEvent event) {
        Main.startScreen();
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


//         
