package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class GameController {
    @FXML
    public Label moneyCounter;
    @FXML
    public TextField moneyInput;
    @FXML
    public Button drawBtn;
    @FXML
    public Button holdBtn;
    @FXML
    public Button doubleBtn;
    @FXML
    public Button betBtn;
    @FXML
    public Button homeBtn;
    @FXML
    public HBox playerContainer;

    IntegerProperty betAmount;
    int money = 100;

    PlayerContent[] players;

    List<Player> listOfPlayers;

    List<Card> dealerCards = new ArrayList<>();

    public void initialize(){

        Socket socket = SessionHandler.getInstance().getSocket();

        ObjectMapper mapper = new ObjectMapper();

        getPlayers(socket);

        // create containers for players and space in fxml

        betAmount = new SimpleIntegerProperty(0);

        startRound();
    }

    public void startRound(){

        Socket socket = SessionHandler.getInstance().getSocket();

        ObjectMapper mapper = new ObjectMapper();

        AtomicReference<String> output = new AtomicReference<>("");

        doubleBtn.setDisable(true);
        drawBtn.setDisable(true);
        homeBtn.setDisable(true);
        holdBtn.setDisable(true);
        betBtn.setDisable(true);

        try {
            // prepare reader
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader

            for(int i = 0; i < players.length+ 1; i++){
                String betsOutput = clientReader.readLine(); // waiting for bet

                MessageContent messageContent = mapper.readValue(betsOutput, MessageContent.class);

                if(messageContent.method().equals("bet")){
                    // i have to bet
                    betBtn.setDisable(false);
                }
                else{
                    // updating
                }
            }

            betAmount.addListener(amount -> {
                output.set(getServerResponse(socket, "bet", "")); // gets Dealer

                try {
                    MessageContent messageContent = mapper.readValue(output.get(), MessageContent.class);

                    String content = messageContent.content();

                    dealerCards.add(mapper.readValue(content, CardContent.class).card());

                    messageContent = mapper.readValue(clientReader.readLine(), MessageContent.class);

                    content = messageContent.content();

                    PlayerCardContent[] playerCardContent = mapper.readValue(content, PlayerCardContent[].class);

                    for (PlayerCardContent player : playerCardContent) {

                        Player newPlayer = new Player(player.name());

                        newPlayer.addCards(player.cards());

                        listOfPlayers.add(newPlayer);
                    }

                    // fill fxml

                    String amIout = clientReader.readLine();

                    MessageContent messageContentOut = mapper.readValue(amIout, MessageContent.class);

                    if(messageContentOut.method().equals("out")){
                        doubleBtn.setDisable(true);
                        drawBtn.setDisable(true);
                        homeBtn.setDisable(true);
                        holdBtn.setDisable(true);
                        betBtn.setDisable(true);

                        waitTillRoundEnd();
                        return;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    clientReader.readLine(); // call
                } catch (IOException e) {
                    e.printStackTrace();
                }

                holdBtn.setDisable(false);
                drawBtn.setDisable(false);
                doubleBtn.setDisable(false);

                betBtn.setDisable(true);

                moneyCounter.setText("Money: " + moneyCounter);

            });
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void drawCard(ActionEvent actionEvent) {

        Socket socket = SessionHandler.getInstance().getSocket();

        doubleBtn.setDisable(true);
        drawBtn.setDisable(true);
        holdBtn.setDisable(true);

        String output = getServerResponse(socket, "hit", "");

        getCard(output);
        continueGame();
    }

    public void doubleBet(ActionEvent actionEvent) {

        Socket socket = SessionHandler.getInstance().getSocket();

        doubleBtn.setDisable(true);
        drawBtn.setDisable(true);
        holdBtn.setDisable(true);

        String output = getServerResponse(socket, "doubledown", "");
        // finished
        getCard(output);
        waitTillRoundEnd();
    }

    public void holdBet(ActionEvent actionEvent) {
        // stay

        Socket socket = SessionHandler.getInstance().getSocket();

        doubleBtn.setDisable(true);
        drawBtn.setDisable(true);
        holdBtn.setDisable(true);

        String output = getServerResponse(socket, "stay", "");
        // finishes round
        continueGame();
    }

    public void placeBet(ActionEvent actionEvent) {

        int amount = 0;

        try {
            amount = Integer.parseInt(moneyInput.getText());
        }
        catch (Exception e){
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setContentText("Only Integers are Allowed");

            alert.showAndWait();
            return;
        }

        if(amount > 100){
            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setContentText("Max 100");

            alert.showAndWait();
            return;
        }

        betAmount.set(amount);
        money -= amount;
    }

    public void goHome(ActionEvent actionEvent) {
        HelloApplication.setStageTo("home.fxml");
    }

    public void getPlayers(Socket socket){
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("getPlayers", "")
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

            players = mapper.readValue(content, PlayerContent[].class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getServerResponse(Socket socket, String method, String content){

        String output = "error";

        try {
            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent(method, content)
            );

            // prepare printStream
            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
            // send json
            printStream.println(jsonString);

            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader
            output = clientReader.readLine();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return output;
    }

    public void getCard(String output){

        Socket socket = SessionHandler.getInstance().getSocket();
        String myName = SessionHandler.getInstance().getUserName();

        ObjectMapper mapper = new ObjectMapper();

        try {
            MessageContent messageContent = mapper.readValue(output, MessageContent.class);

            String content = messageContent.content();

            Card extraCard = mapper.readValue(content, Card.class);

            Player myPlayer = listOfPlayers.stream().filter(
                    player -> player.getName().equals(myName)).findFirst().get();

            myPlayer.addCard(extraCard);

            // display card

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void continueGame(){

        Socket socket = SessionHandler.getInstance().getSocket();
        ObjectMapper mapper = new ObjectMapper();

        try{
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String output = clientReader.readLine();

            MessageContent messageContent = mapper.readValue(output, MessageContent.class);

            if(messageContent.method().equals("out")) {
                doubleBtn.setDisable(true);
                drawBtn.setDisable(true);
                homeBtn.setDisable(true);
                holdBtn.setDisable(true);
                betBtn.setDisable(true);

                waitTillRoundEnd();
            }
            else {
                drawBtn.setDisable(false);
                holdBtn.setDisable(false);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void waitTillRoundEnd(){

    }
}
