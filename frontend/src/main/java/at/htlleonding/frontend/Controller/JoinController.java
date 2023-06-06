package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class JoinController {
    @FXML
    public Button publicButton;
    @FXML
    public Button privateButton;

    public void publicLobby(ActionEvent actionEvent){
        HelloApplication.setStageTo("joinPublic.fxml");
    }

    public void privateLobby(ActionEvent actionEvent) {
        HelloApplication.setStageTo("joinPrivate.fxml");
    }
}
