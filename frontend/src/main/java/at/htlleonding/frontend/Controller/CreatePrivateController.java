package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.CreateContent;
import at.htlleonding.frontend.model.MessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class CreatePrivateController {
    @FXML
    public Slider roundSlider;
    @FXML
    public Slider playerSlider;
    @FXML
    public Button createButton;
    @FXML
    public TextField pwdfield;

    public void createRoom(ActionEvent actionEvent) {

        int rounds = (int) roundSlider.getValue();
        int players = (int) playerSlider.getValue();

        String password = pwdfield.getText();

        // enter room but in waiting mode
        Socket socket = SessionHandler.getInstance().getSocket();

        try {

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("open", mapper.writeValueAsString(new CreateContent(
                            String.valueOf(rounds),
                            String.valueOf(players),
                            SessionHandler.getInstance().getUserName(),
                            password)))
            );

            // prepare printStream
            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
            // send json
            printStream.println(jsonString);
            // prepare reader
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader
            String output = clientReader.readLine(); // first time I get weird response

            output = clientReader.readLine();

            System.out.println(output);

            if(output != null && output.equals("Success")){
                // load into game
                HelloApplication.setStageTo("game.fxml");
            }
            else{
                Alert full = new Alert(Alert.AlertType.INFORMATION, "Sorry something went wrong");

                full.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
