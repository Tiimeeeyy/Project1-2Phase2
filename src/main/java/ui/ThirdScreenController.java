package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ThirdScreenController {

    @FXML
    private ImageView mapImageView;

    @FXML
    private Button goBackButton;

    @FXML
    private Slider directionSlider;

    @FXML
    private Slider powerSlider;

    @FXML
    public void initialize() {
        Image image = new Image(getClass().getResource("/newMap.PNG").toExternalForm());
        mapImageView.setImage(image);

        // Add listeners or handlers if needed
        // Example: directionSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateDirection(newVal));
        // Example: powerSlider.valueProperty().addListener((obs, oldVal, newVal) -> updatePower(newVal));
    }

    @FXML
    private void goBack() {
        Main.openGUI();
    }

    // Additional methods to handle slider values (if needed)
    // private void updateDirection(Number newValue) {
    //     // Implement logic to handle direction changes
    // }

    // private void updatePower(Number newValue) {
    //     // Implement logic to handle power changes
    // }
}
