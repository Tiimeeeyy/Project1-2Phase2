package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ui.controller.MapPageController;
import ui.controller.ThirdScreenController;
import ui.screenFactory.ScreenFactory;
import ui.screenFactory.ScreenInterface;

import java.io.IOException;

public class Main extends Application {
    private static Stage primaryStage;    

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/design/images/appIcon.jpg"))); 
        setScreen("START", "", 0, 0, 0, 0, 0, 0, 0, 0, null, null);
    }

    // use factory here
    public void setScreen(String screenType, String function, double xBall, double yBall, double xHole, double yHole, double radiusHole, double treeRadius, double grassFrictionKINETIC, double grassFrictionSTATIC, double[] startBallPosition, double[] holePosition ){
        try{
            ScreenFactory factory = new ScreenFactory();
            ScreenInterface screen = factory.createScreen(screenType, function, xBall, yBall, xHole, yHole, radiusHole, treeRadius, grassFrictionKINETIC, grassFrictionSTATIC, startBallPosition, holePosition);
            System.out.println(screen.getRoot());
            Scene scene = new Scene(screen.getRoot(), 1280, 720);
            primaryStage.setTitle("Golf Game!");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch(Exception e){
            e.printStackTrace();
        
    }}

    public static void main(String[] args) {
        launch(args);
    }
}
