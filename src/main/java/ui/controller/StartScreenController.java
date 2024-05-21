package ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;
import ui.Main;

public class StartScreenController {

    @FXML
    private void startGame(ActionEvent event) {
        Main.openGUI();
    }

    @FXML
    private void exitGame(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
