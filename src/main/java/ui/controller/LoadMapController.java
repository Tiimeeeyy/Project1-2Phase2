package ui.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox; // Add this import statement
import ui.Main;
import ui.screenFactory.ScreenInterface;

/**
 * The LoadMapController class is responsible for handling the map loading screen's 
 * functionality, including navigation and map selection.
 */
public class LoadMapController implements ScreenInterface {

    private Parent root; // Root node

    @FXML
    private TabPane mapTabPane; // The TabPane containing the maps

    /**
     * Constructs a LoadMapController instance.
     */
    public LoadMapController() {
    }

    /**
     * Gets the root node of this controller.
     *
     * @return the root node
     */
    @Override
    public Parent getRoot() {
        return root;
    }

    /**
     * Sets the root node for this controller.
     *
     * @param root the root node to set
     */
    public void setRoot(Parent root) {
        this.root = root;
    }

    /**
     * Handles the action of going to the next screen.
     * Retrieves the selected map's image URL and sets the next screen with the map's path.
     *
     * @param event the action event triggered by the user
     */
    @FXML
    private void goNext(ActionEvent event) {
        Tab selectedTab = mapTabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            ImageView mapImageView = (ImageView) ((HBox) selectedTab.getContent()).getChildren().get(0);
            String imageUrl = mapImageView.getImage().getUrl();
            String relativePath = getRelativePath(imageUrl);
            System.out.println("Selected Map Image Path: " + relativePath);
            Main mainInst = new Main();
            mainInst.setScreen("INPUT2", relativePath, 0, 0, 0, 0, 0, 0, 0, 0, null, null);
        }
    }

    /**
     * Handles the action of going back to the previous screen.
     *
     * @param event the action event triggered by the user
     */
    @FXML
    private void goBack(ActionEvent event) {
        Main mainInst = new Main();
        mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    /**
     * Converts an absolute image URL to a relative path used in the application.
     *
     * @param imageUrl the absolute image URL
     * @return the relative path of the image
     */
    private String getRelativePath(String imageUrl) {
        String targetPrefix = "target/classes/";
        int targetIndex = imageUrl.indexOf(targetPrefix);
        if (targetIndex != -1) {
            return "src/main/resources/" + imageUrl.substring(targetIndex + targetPrefix.length());
        } else {
            return imageUrl;
        }
    }
}
