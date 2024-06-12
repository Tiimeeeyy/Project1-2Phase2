package ui.screenFactory;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import ui.controller.FirstScreenController;
import ui.controller.InputSecondController;
import ui.controller.LoadMapController;
import ui.controller.MapPageController;
import ui.controller.StartScreenController;
import ui.controller.ThirdScreenController;

/**
 * Factory class to create different screens for the application.
 */
public class ScreenFactory {

    /**
     * Creates a screen based on the specified type.
     *
     * @param screenType            the type of the screen
     * @param function              the function for map generation (for MAP screen)
     * @param xBall                 the x-coordinate of the ball (for MAP screen)
     * @param yBall                 the y-coordinate of the ball (for MAP screen)
     * @param xHole                 the x-coordinate of the hole (for MAP screen)
     * @param yHole                 the y-coordinate of the hole (for MAP screen)
     * @param radiusHole            the radius of the hole (for MAP and GAME screens)
     * @param treeRadius            the radius of trees (for MAP screen)
     * @param grassFrictionKINETIC  the kinetic friction of grass (for MAP and GAME screens)
     * @param grassFrictionSTATIC   the static friction of grass (for MAP and GAME screens)
     * @param startBallPosition     the starting position of the ball (for GAME screen)
     * @param holePosition          the position of the hole (for GAME screen)
     * @return the screen controller
     * @throws Exception if the screen type is invalid or the screen creation fails
     */
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
            } else if (screenType.equals("LOAD")) {
                fxmlLoader.setLocation(getClass().getResource("/design/LoadMap.fxml"));
                root = fxmlLoader.load();
                LoadMapController controller = fxmlLoader.getController();
                controller.setRoot(root);
                return controller;
             } else if (screenType.equals("INPUT2")) {
                fxmlLoader.setLocation(getClass().getResource("/design/InputSecondController.fxml"));
                root = fxmlLoader.load();
                InputSecondController controller = fxmlLoader.getController();
                controller.setRoot(root);
                return controller;
             }else {
                throw new Exception("Invalid screen type");
            }
        } catch (Exception e) {
            throw new Exception("Failed to create screen", e);
        }
    }
}
