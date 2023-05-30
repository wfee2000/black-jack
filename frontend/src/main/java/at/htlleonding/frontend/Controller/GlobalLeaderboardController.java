package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.SocketHandler.SocketHandler;
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

        System.out.println("GlobalLeaderboardController.initialize()");

        Socket socket = SocketHandler.getInstance().getSocket();

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
            // output returning message
            System.out.println(output);

            MessageContent messageContent = mapper.readValue(output, MessageContent.class);

            String content = messageContent.content();

            EntryModel[] entryContent = mapper.readValue(content, EntryModel[].class);

            leaderboard.setItems(FXCollections.observableList(Arrays.stream(entryContent).toList()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("GlobalLeaderboardController.initialize() end");
    }

    public void backToHome(ActionEvent actionEvent) {

        try {
            Parent window = FXMLLoader.load(Objects.requireNonNull(this.getClass()
                    .getResource("/at/htlleonding/frontend/home.fxml")));

            Scene newScene = new Scene(window);
            Stage mainWindow = (Stage) content.getScene().getWindow();
            mainWindow.setScene(newScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
