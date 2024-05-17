package ui;

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

    private String function;

    public void nextScreen(ActionEvent event) {
        function = FunctionTextfield.getText();
        double xBall, yBall, xHole, yHole;
        try {
            xBall = Double.parseDouble(X_BALL.getText());
            yBall = Double.parseDouble(Y_BALL.getText());
            xHole = Double.parseDouble(X_HOLE.getText());
            yHole = Double.parseDouble(Y_HOLE.getText());
        } catch (NumberFormatException e) {
            showAlert("Error!", "Invalid input for coordinates", "Please enter valid numbers for the coordinates.");
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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MapPage.fxml"));
            MapPageController controller = new MapPageController(function, xBall, yBall, xHole, yHole);
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Map Creating Screen");
            stage.show();
        } catch (IOException ex) {
            showAlert("Error!", "Failed to proceed to the next screen", ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
