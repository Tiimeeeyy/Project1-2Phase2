package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ui.screenFactory.ScreenFactory;
import ui.screenFactory.ScreenInterface;

import java.io.IOException;

/**
 * Main class for the application.
 */
public class Main extends Application {
    private static Stage primaryStage; // Primary stage for the application

    /**
     * Starts the JavaFX application.
     *
     * @param stage the primary stage
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/design/images/appIcon.jpg")));
        setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    /**
     * Sets the screen using the ScreenFactory.
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
     */
    public void setScreen(String screenType, String function, double xBall, double yBall, double xHole, double yHole, double radiusHole, double treeRadius, double grassFrictionKINETIC, double grassFrictionSTATIC, double[] startBallPosition, double[] holePosition) {
        try {
            ScreenFactory factory = new ScreenFactory();
            ScreenInterface screen = factory.createScreen(screenType, function, xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC, startBallPosition, holePosition);
            Scene scene = new Scene(screen.getRoot(), 1280, 720);
            primaryStage.setTitle("Golf Game!");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
