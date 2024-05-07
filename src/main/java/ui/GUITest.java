package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class GUITest extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(GUITest.class.getResource("GUI.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        stage.setTitle("Graphic User Interface (in development)");
        stage.setScene(scene);
        stage.show();

        Tokeniser tokeniser = new Tokeniser();
        GUIcontroller controller = fxmlLoader.getController();
        controller.setTokeniser(tokeniser);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
