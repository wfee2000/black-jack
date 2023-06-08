package at.htlleonding.frontend.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;

public class CreatePublicController {
    @FXML
    public Button createButton;
    @FXML
    public Slider roundSlider;
    @FXML
    public Slider playerSlider;

    public void createRoom(ActionEvent actionEvent) {

        int rounds = (int) roundSlider.getValue();
        int players = (int) playerSlider.getValue();

        // enter room but in waiting mode
    }
}
