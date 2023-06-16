package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.MessageContent;
import at.htlleonding.frontend.model.RoomContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

        Socket socket = SessionHandler.getInstance().getSocket();

        if(name.isEmpty() || password.isEmpty() ||
                name.chars().allMatch(c -> c == (int)' ') || password.chars().allMatch(c -> c == (int)' ')){

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setContentText("Empty username or password!");

            alert.showAndWait();

            return;
        }

        try {

            RoomContent room = getRoom(socket, name, password);

            if (room == null){
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setContentText("Wrong lobbyname or password!");

                alert.showAndWait();
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("join", room.id())
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


            if (output != null && output.equals("Success")) {
                HelloApplication.setStageTo("game.fxml");
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setContentText("Something went wrong");

                alert.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RoomContent getRoom(Socket socket, String name, String password){

        try {

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("rooms", mapper.writeValueAsString(""))
            );

            // prepare printStream
            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
            // send json
            printStream.println(jsonString);
            // prepare reader
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader
            String output = clientReader.readLine();

            MessageContent messageContent = mapper.readValue(output, MessageContent.class);

            String content = messageContent.content();

            RoomContent[] allRooms = mapper.readValue(content, RoomContent[].class);

            for (RoomContent room : allRooms) {
                if(room.password().equals(password) && room.name().equals(name))
                {
                    return room;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
