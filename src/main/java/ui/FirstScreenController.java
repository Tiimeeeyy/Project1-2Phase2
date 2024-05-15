package ui;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import engine.parser.ExpressionParser;

public class FirstScreenController {
    
    @FXML
    private TextField FunctionTextfield;
    private String function;
    

    public void nextScreen(ActionEvent event) {
        function = FunctionTextfield.getText();

        try {
            // 255 - ((0.4 * (0.9 - e^(-(((x / 50 - 5)^2 + (y / 50 - 5)^2) / 8)))) * 200 + 80)
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MapPage.fxml"));
            fxmlLoader.setController(new MapPageController(function));
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Map Creating Screen");
            stage.show();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error!");
            alert.setHeaderText("Failed to proceed to the next screen, check the input values");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            ex.printStackTrace();
        }
    }


}