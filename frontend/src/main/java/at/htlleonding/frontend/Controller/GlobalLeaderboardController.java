package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.EntryModel;
import at.htlleonding.frontend.model.MessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.Objects;

public class GlobalLeaderboardController {
    @FXML
    public ListView leaderboard = new ListView<>();
    public GridPane content;

    public void initialize() {

        Socket socket = SessionHandler.getInstance().getSocket();

        try {

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("globalleaderboard", mapper.writeValueAsString(""))
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

            EntryModel[] entryContent = mapper.readValue(content, EntryModel[].class);

            leaderboard.setItems(FXCollections.observableList(Arrays.stream(entryContent).toList()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void backToHome(ActionEvent actionEvent) {

        try {
            HelloApplication.setStageTo("home.fxml");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
