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
        openGUI();
    }

    public static void openGUI() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/GUI.fxml"));
            fxmlLoader.setController(new GUIcontroller());
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            primaryStage.setTitle("Graphic User Interface (in development)");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}