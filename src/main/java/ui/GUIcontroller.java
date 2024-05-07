package ui;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;

import java.lang.classfile.Label;
import java.util.HashMap;
import java.util.Map;
import engine.parser.ExpressionParser; // Import the ExpressionParser class

public class GUIcontroller {
    
    @FXML
    private TextField FunctionTextfield;
    private Label parsedResultLabel;
    
    public void parseFunction(ActionEvent e) {
        String function = FunctionTextfield.getText();
        
        // Initialize variables map with empty
        Map<String, Double> variables = new HashMap<>();
        
        // Create an instance of ExpressionParser
        ExpressionParser parser = new ExpressionParser(function, variables);
        
        try {
            // Parse the function
            double result = parser.evaluate();
            // Display the parsed result (you can modify this according to your UI)
            System.out.println("Parsed result: " + result);
            ((TextInputControl) parsedResultLabel).setText("Parsed result: " + result);
        } catch (Exception ex) {
            // Display an error message if parsing fails
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Parsing Error");
            alert.setHeaderText("Failed to parse function");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();

            ((TextInputControl) parsedResultLabel).setText("Parsing error: " + ex.getMessage());
        }
    }
}
