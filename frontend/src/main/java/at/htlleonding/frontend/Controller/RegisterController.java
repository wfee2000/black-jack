package at.htlleonding.frontend.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class RegisterController {
    @FXML
    public GridPane content;
    @FXML
    private TextField nameField;
    @FXML
    private TextField pwdField;


    public void registerUser(ActionEvent actionEvent) {
        // an Socket senden

        String name = nameField.getText();
        String pwd = pwdField.getText();
    }
}
