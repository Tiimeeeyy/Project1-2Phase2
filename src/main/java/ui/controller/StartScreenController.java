package ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import ui.Main;
import ui.screenFactory.ScreenInterface;

public class StartScreenController implements ScreenInterface{

    private Parent root;

    public void setRoot(Parent root) {
        this.root = root;
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @FXML
    private void startGame(ActionEvent event) {
        Main mainInst = new Main();
        mainInst.setScreen("INPUT", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    @FXML
    private void exitGame(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
