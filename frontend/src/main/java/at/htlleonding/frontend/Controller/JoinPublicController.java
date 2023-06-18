package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.EntryContent;
import at.htlleonding.frontend.model.MessageContent;
import at.htlleonding.frontend.model.RoomContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinPublicController {
    @FXML
    public ListView lobbyListView;

    public void initialize(){
        Socket socket = SessionHandler.getInstance().getSocket();

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

            List<RoomContent> publicRooms = new ArrayList<>();

            for (RoomContent room : allRooms) {
                if(room.password().equals("")){publicRooms.add(room);}
            }

            lobbyListView.setItems(FXCollections.observableList(publicRooms));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void lobbySelected(KeyEvent keyEvent) {

        if(keyEvent.getCode() != KeyCode.ENTER){
            return;
        }

        RoomContent room = (RoomContent) lobbyListView.getSelectionModel().getSelectedItem();

        int id =  Integer.parseInt(room.id());

        Socket socket = SessionHandler.getInstance().getSocket();

        try {

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("join", mapper.writeValueAsString(id))
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

            if(output != null && output.equals("Success")){
                HelloApplication.setStageTo("waitingRoom.fxml");
            }
            else{
                Alert full = new Alert(Alert.AlertType.INFORMATION, "It seems like the lobby is full");

                full.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
