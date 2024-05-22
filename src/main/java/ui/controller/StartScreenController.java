package ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import ui.Main;
import ui.screenFactory.ScreenInterface;

/**
 * Controller for the start screen.
 */
public class StartScreenController implements ScreenInterface {

    private Parent root; // Root node of the screen

    /**
     * Sets the root node of the screen.
     *
     * @param root the root node
     */
    public void setRoot(Parent root) {
        this.root = root;
    }

    /**
     * Gets the root node of the screen.
     *
     * @return the root node
     */
    @Override
    public Parent getRoot() {
        return root;
    }

    /**
     * Handles the action to start the game.
     *
     * @param event the action event
     */
    @FXML
    private void startGame(ActionEvent event) {
        Main mainInst = new Main();
        mainInst.setScreen("INPUT", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    /**
     * Handles the action to exit the game.
     *
     * @param event the action event
     */
    @FXML
    private void exitGame(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }
}
