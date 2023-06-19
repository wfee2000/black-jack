package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Objects;

public class HomeController {
    @FXML
    public GridPane content;

    public void loadLeaderboard(ActionEvent actionEvent) {
        HelloApplication.setStageTo("globalLeaderboard.fxml");
    }

    public void joinGame(ActionEvent actionEvent) {
        HelloApplication.setStageTo("join.fxml");
    }

    public void createGame(ActionEvent actionEvent) {
        HelloApplication.setStageTo("create.fxml");
    }
}
