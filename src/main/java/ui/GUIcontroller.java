package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class GUIcontroller {
    // TODO: remove the four input fields, only make one + parse it + display it invisibly
    @FXML
    private TextField FunctionTextfield;
    
    public void Function(ActionEvent e) {
        String function = FunctionTextfield.getText();
        System.out.println(function);
    }

    public void setTokeniser(Tokeniser tokeniser) {
        this.tokeniser = tokeniser;
    }
}
