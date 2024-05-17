package ui;

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
    private TextField xCoordinateTextField;
    @FXML
    private TextField yCoordinateTextField;

    public void nextScreen(ActionEvent event) {
        String function = FunctionTextfield.getText();
        try {
            double x = Double.parseDouble(xCoordinateTextField.getText());
            double y = Double.parseDouble(yCoordinateTextField.getText());
            if (x < 0 || x > 900 || y < 0 || y > 600) {
                throw new IllegalArgumentException("Invalid coordinates. Allowed coordinates: X = 0 - 900; Y = 0 - 600.");
            }
            System.out.println("X Coordinate: " + x + ", Y Coordinate: " + y);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MapPage.fxml"));
            fxmlLoader.setController(new MapPageController(function));
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Map Creating Screen");
            stage.show();
        } catch (NumberFormatException | IOException ex) {
            showErrorAlert("Invalid input. Please enter numeric values for coordinates.");
        } catch (IllegalArgumentException ex) {
            showErrorAlert(ex.getMessage());
        }
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText("Failed to proceed to the next screen.");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
