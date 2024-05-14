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
import java.util.HashMap;
import java.util.Map;
import engine.parser.ExpressionParser;

public class FirstScreenController {
    
    @FXML
    private TextField FunctionTextfield;
    private Label parsedResultLabel;
    
    public void parseFunction(ActionEvent e) {
        String function = FunctionTextfield.getText();
        Map<String, Double> variables = new HashMap<>();
        ExpressionParser parser = new ExpressionParser(function, variables);
        
        try {
            double result = parser.evaluate();
            System.out.println("Parsed result: " + result);
           parsedResultLabel.setText("Parsed result: " + result);
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Parsing Error");
            alert.setHeaderText("Failed to parse function");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
           parsedResultLabel.setText("Parsing error: " + ex.getMessage());
        }
    }

    public void nextScreen(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/MapPage.fxml"));
            fxmlLoader.setController(new MapPageController());
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Map");
            stage.show();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }    
}