package ui.screenFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ui.controller.FirstScreenController;
import ui.controller.MapPageController;
import ui.controller.StartScreenController;
import ui.controller.ThirdScreenController;

public class ScreenFactory {
    public ScreenInterface createScreen(String screenType, String function, double xBall, double yBall, double xHole, double yHole, double radiusHole, double treeRadius, double grassFrictionKINETIC, double grassFrictionSTATIC, double[] startBallPosition, double[] holePosition) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Parent root;
            if (screenType.equals("START")) {
                fxmlLoader.setLocation(getClass().getResource("/design/StartScreen.fxml"));
                root = fxmlLoader.load();
                StartScreenController controller = fxmlLoader.getController();
                controller.setRoot(root);
                return controller;
            } else if (screenType.equals("INPUT")) {
                fxmlLoader.setLocation(getClass().getResource("/design/FirstScreen.fxml"));
                root = fxmlLoader.load();
                FirstScreenController controller = fxmlLoader.getController();
                controller.setRoot(root);
                return controller;
            } else if (screenType.equals("MAP")) {
                fxmlLoader.setLocation(getClass().getResource("/design/MapPage.fxml"));
                root = fxmlLoader.load();
                MapPageController controller = fxmlLoader.getController();
                controller.setRoot(root);
                controller.initializeParameters(function, xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC);
                return controller;
            } else if (screenType.equals("GAME")) {
                fxmlLoader.setLocation(getClass().getResource("/design/ThirdScreen.fxml"));
                root = fxmlLoader.load();
                ThirdScreenController controller = fxmlLoader.getController();
                controller.setRoot(root);
                controller.initializeParameters(startBallPosition, holePosition, radiusHole, grassFrictionKINETIC, grassFrictionSTATIC);
                return controller;
            } else {
                throw new Exception("Invalid screen type");
            }
        } catch (Exception e) {
            throw new Exception("Failed to create screen", e);
        }
    }
}
