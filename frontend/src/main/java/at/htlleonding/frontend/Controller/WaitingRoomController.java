package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.EntryContent;
import at.htlleonding.frontend.model.MessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;

public class WaitingRoomController extends Thread{
    @FXML
    public Label playerCount;
    @FXML
    public volatile Button startButton;

    String output = "123";
    boolean isRoomFull = false;

    public WaitingRoomController(){

    }

    public void initialize(){
        startButton.setDisable(true);

        Thread checkThread = new Thread(this);

        checkThread.start();
    }

    public void startGame(ActionEvent actionEvent) {
        HelloApplication.setStageTo("game.fxml");
    }


    @Override
    public void run() {
        Socket socket = SessionHandler.getInstance().getSocket();

        try {

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("roomstate", mapper.writeValueAsString(""))
            );

            while (!isRoomFull){
                // prepare printStream
                PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
                // send json
                printStream.println(jsonString);
                // prepare reader
                BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                // wait for reader

                Platform.runLater(() -> {
                    try {
                        output = clientReader.readLine();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (output.length() == 3 && output.contains("/")) {
                        playerCount.setText("[" + output + "]");
                        isRoomFull = output.split("/")[0].equals(output.split("/")[1]);
                    }
                });
            }

            startButton.setDisable(false);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
