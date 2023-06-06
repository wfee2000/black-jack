package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.SocketHandler.SocketHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.net.Socket;

public class JoinPrivateController {

    @FXML
    public TextField nameField;
    @FXML
    public TextField pwdField;

    public void join(ActionEvent actionEvent) {

        // check if Lobby exists
        //throe allert

        String name = nameField.getText();
        String password = pwdField.getText();
        Socket socket = SocketHandler.getInstance().getSocket();

        if(name.isEmpty() || password.isEmpty() ||
                name.chars().allMatch(c -> c == (int)' ') || password.chars().allMatch(c -> c == (int)' ')){

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setContentText("Empty username or password!");

            alert.showAndWait();

            return;
        }

        try {

            // check with database

            var output = "";

            if (output != null && output.equals("Connected")) {
                //HelloApplication.setStageTo("home.fxml");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setContentText("Wrong lobbyname or password!");

                alert.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
