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

public class LoadMapController implements ScreenInterface {

    private Parent root; // Root node

    @FXML
    private TabPane mapTabPane; // The TabPane containing the maps

    public LoadMapController() {
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    public void setRoot(Parent root) {
        this.root = root;
    }

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

    @FXML
    private void goBack(ActionEvent event) {
        Main mainInst = new Main();
        mainInst.setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    private String getRelativePath(String imageUrl) {
        String targetPrefix = "target/classes/";
        int targetIndex = imageUrl.indexOf(targetPrefix);
        if (targetIndex != -1) {
            String relativePath = "src/main/resources/" + imageUrl.substring(targetIndex + targetPrefix.length());
            return relativePath;
        } else {
            return imageUrl;
        }
    }
}
