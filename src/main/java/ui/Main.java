package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        startScreen();
    }

    public static void startScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/StartScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 613, 404);
            primaryStage.setTitle("Title Screen");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openGUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/FirstScreen.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            primaryStage.setTitle("First Screen");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openThirdScreen(double[] startBallPosition, double[] holePosition, double radiusHole, double grassFrictionKINETIC, double grassFrictionSTATIC) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/ThirdScreen.fxml"));
            ThirdScreenController controller = new ThirdScreenController(startBallPosition, holePosition, radiusHole, grassFrictionKINETIC, grassFrictionSTATIC);
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
            primaryStage.setTitle("Third Screen");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
