package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import javafx.event.ActionEvent;

public class CreateController {
    public void publicLobby(ActionEvent actionEvent) {
        HelloApplication.setStageTo("createPublic.fxml");
    }

    public void privateLobby(ActionEvent actionEvent) {
        HelloApplication.setStageTo("createPrivate.fxml");
    }
}
