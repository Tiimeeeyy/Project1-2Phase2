package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class GUIcontroller {
    // TODO: Implement the user interface
    // Calls to the objects in the FXML
    @FXML
    private TextField FunctionTextfield;

    public void Function(ActionEvent e){
        String function = FunctionTextfield.getText();

        if (!parser.ExpressionParser()){ //TODO: check parser to fix this ifelse statement
           return
        } else System.out.println(function); 
    }

    //TODO: Connect main method variables with the four textfields
    //variables accepts vars as str and double so cant do it the same as expression (which is a str)
    //might wanna convert the strings it accepts into doubles
}
