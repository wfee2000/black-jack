package at.htlleonding.frontend.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

public class CreatePrivateController {
    @FXML
    public Slider roundSlider;
    @FXML
    public Slider playerSlider;
    @FXML
    public Button createButton;

    public void createRoom(ActionEvent actionEvent) {

        int rounds = (int) roundSlider.getValue();
        int players = (int) playerSlider.getValue();

        // enter room but in waiting mode
    }
}
