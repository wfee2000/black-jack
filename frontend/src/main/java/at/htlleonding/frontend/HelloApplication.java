package at.htlleonding.frontend;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static Stage grabStage;

    @Override
    public void start(Stage stage) throws IOException {
        grabStage = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 540);
        stage.setTitle("Blackjack");
        stage.setScene(scene);
        stage.show();
    }

    public static void setStageTo(String path) {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource(path));
        Scene scene;

        try {
            if(path.equals("game.fxml")){
                scene = new Scene(fxmlLoader.load(), 1280, 720);
            }else{
                scene = new Scene(fxmlLoader.load(), 960, 540);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        grabStage.setTitle("Blackjack");
        grabStage.setScene(scene);
        grabStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}