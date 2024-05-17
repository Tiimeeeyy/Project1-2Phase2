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
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/FirstScreen.fxml"));
            fxmlLoader.setController(new FirstScreenController());
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            primaryStage.setTitle("First Screen");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void openThirdScreen() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/ThirdScreen.fxml"));
            fxmlLoader.setController(new ThirdScreenController());
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            primaryStage.setTitle("Third Screen");
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