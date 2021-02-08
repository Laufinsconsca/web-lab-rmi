package pkg;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Init extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/main.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("Convolve an image");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

}