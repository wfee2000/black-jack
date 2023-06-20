package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static at.htlleonding.frontend.SessionHandler.SessionHandler.*;

public class GameController {
    @FXML
    public Label moneyCounter;
    @FXML
    public HBox playerContainer;
    @FXML
    public Button homeBtn;
    @FXML
    public HBox dealerBox;

    int money = 100;

    int betAmount;

    PlayerContent[] players;

    List<Player> listOfPlayers;

    List<Card> dealerCards = new ArrayList<>();

    Scene scene;

    public void initialize(){
        Socket socket = getInstance().getSocket();

        scene = dealerBox.getScene();

        getPlayers(socket);

        for (PlayerContent player : players) {
            VBox playerBox = new VBox();

            playerBox.setPrefWidth(1280 - (double) (10 * (players.length - 1)) / players.length);
            playerBox.setId(player.name());
            playerBox.setAlignment(Pos.CENTER);

            Label nameOfPlayer = new Label();
            nameOfPlayer.setId(player.name() + "name");

            Label betOfPlayer = new Label();
            betOfPlayer.setId(player.name() + "bet");

            HBox cardContainer = new HBox();
            cardContainer.setId(player.name() + "cards");
            cardContainer.setAlignment(Pos.CENTER);

            playerBox.getChildren().add(nameOfPlayer);
            playerBox.getChildren().add(betOfPlayer);
            playerBox.getChildren().add(cardContainer);

            playerContainer.getChildren().add(playerBox);
        }

        startRound();
    }

    public void startRound() {
        Socket socket = SessionHandler.getInstance().getSocket();
        // prepare reader
        BufferedReader clientReader = null;
        try {
            clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (clientReader == null) {
            return;
        }
        // wait for reader

        processBets(socket, clientReader);
        moneyCounter.setText("Money: " + money);

        dealerCards.add(getDealersCard(clientReader));
        listOfPlayers = getDistributedPlayerCards(clientReader);

        // fill fxml
        updatePlayersFxml();
        updateDealersFxml();

        if (isPlayerOut(clientReader)) {
            waitTillRoundEnd();
            return;
        }

        MessageContent messageContent = null;

        do {
            try {
                messageContent = unwrapContent(clientReader.readLine(), MessageContent.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            switch (Objects.requireNonNull(messageContent).method()) {
                case "call" -> {
                    if (isPlayerOut(sendCall(socket, getCall()))) {
                        waitTillRoundEnd();
                        return;
                    }
                }
                case "hit", "doubleDown" -> {
                    PlayerCardContent playerCardContent =
                            unwrapContent(messageContent.content(), PlayerCardContent.class); // one card

                    listOfPlayers.stream().filter(
                            player -> player.getName().equals(playerCardContent.name())
                    ).findFirst().orElseThrow().getCards().add(playerCardContent.cards()[0]);

                    updatePlayersFxml();
                }
            }

            updatePlayersFxml();
        } while (true);
    }

    public void updatePlayersFxml(){
        for (Node box : playerContainer.getChildren()) {

            Player playerToFill = listOfPlayers.stream().filter(
                    player -> player.getName().equalsIgnoreCase(box.getId())
            ).findFirst().orElseThrow();

            Label playerBet = (Label) scene.lookup(playerToFill.getName() + "bet");
            Label playerName = (Label) scene.lookup(playerToFill.getName() + "name");

            playerBet.setText(Integer.toString(playerToFill.getBet()));
            playerName.setText(playerToFill.getName());

            // set cards

            HBox cardContainer = (HBox) scene.lookup(playerToFill.getName() + "cards");

            cardContainer.getChildren().removeAll();

            for (Card card: playerToFill.getCards()) {
                ImageView cardImg;

                String cardValue = getStringValueOfCard(card);

                cardImg = new ImageView(new Image(
                        Objects.requireNonNull(getClass().getResourceAsStream(
                                "card/" + card.sign() + "/" + cardValue + ".svg")))
                );

                cardContainer.getChildren().add(cardImg);
            }
        }
    }

    public void updateDealersFxml(){
        dealerBox.getChildren().removeAll();

        for(Card card: dealerCards){
            ImageView cardImg;

            String cardValue = getStringValueOfCard(card);

            cardImg = new ImageView(new Image(
                    Objects.requireNonNull(getClass().getResourceAsStream(
                            "card/" + card.sign() + "/" + cardValue + ".svg")))
            );

            dealerBox.getChildren().add(cardImg);
        }
    }

    public String getStringValueOfCard(Card card){

        return switch (card.value()) {
            case A -> "14";
            case _2 -> "2";
            case _3 -> "3";
            case _4 -> "4";
            case _5 -> "5";
            case _6 -> "6";
            case _7 -> "7";
            case _8 -> "8";
            case _9 -> "9";
            case _10 -> "10";
            case J -> "11";
            case Q -> "12";
            case K -> "13";
        };
    }

    private String sendCall(Socket socket, Call call) {
        return getServerResponse(socket, call.name(), "");
    }

    private boolean isPlayerOut(BufferedReader clientReader) {
        String isOut = null;
        try {
            isOut = clientReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isPlayerOut(isOut);
    }

    private boolean isPlayerOut(String input) {
        MessageContent messageContentOut = unwrapContent(input, MessageContent.class);
        return messageContentOut.method().equalsIgnoreCase("out");
    }

    private List<Player> getDistributedPlayerCards(BufferedReader clientReader) {
        MessageContent messageContent = null;
        try {
            messageContent = unwrapContent(clientReader.readLine(), MessageContent.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert messageContent != null;
        String content = messageContent.content();
        PlayerCardContent[] playerCardContent = unwrapContent(content, PlayerCardContent[].class);
        List<Player> players = new ArrayList<>();

        for (PlayerCardContent player : playerCardContent) {

            Player newPlayer = new Player(player.name());

            newPlayer.addCards(player.cards());

            players.add(newPlayer);
        }

        return players;
    }

    private Card getDealersCard(BufferedReader clientReader) {
        String output = null; // gets Dealer
        try {
            output = clientReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        MessageContent messageContent = unwrapContent(output, MessageContent.class);

        String content = messageContent.content();

        return unwrapContent(content, CardContent.class).card();
    }

    private void processBets(Socket socket, BufferedReader clientReader) {
        for(int i = 0; i < players.length; i++){
            String betsOutput = null; // waiting for bet
            try {
                betsOutput = clientReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MessageContent messageContent = unwrapContent(betsOutput, MessageContent.class);

            if (messageContent.method().equals("bet")){
                // I have to bet

                betAmount = getBet();
                money -= betAmount;
                getServerResponse(socket, "bet", wrapObject(new BetContent(betAmount)));
            } else {
                // other have to bet
                PlayerBetContent betContent = unwrapContent(messageContent.content(), PlayerBetContent.class);
                listOfPlayers.stream().filter(player -> player.getName().equals(betContent.name())).findFirst()
                        .orElseThrow().setBet(betContent.bet());
                updatePlayersFxml();
            }
        }
    }

    private int getBet() {
        Dialog<Integer> dialog = new Dialog<>();
        ButtonType betButtonType = new ButtonType("Bet", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().setAll(betButtonType);

        TextField moneyInput = new TextField();
        Label error = new Label();
        error.setTextFill(Color.RED);
        VBox vBox = new VBox();
        vBox.getChildren().setAll(error, moneyInput);
        Node betButton = dialog.getDialogPane().lookupButton(betButtonType);
        betButton.setDisable(true);

        BooleanBinding isBet = Bindings.createBooleanBinding(
            () -> {
                int moneyBet;

                try {
                    moneyBet = Integer.parseInt(moneyInput.getText());

                    if (moneyBet > 100) {
                        error.setText("Max 100");
                        return false;
                    } else if (moneyBet == 0) {
                        error.setText("Min 1");
                        return false;
                    }

                } catch (NumberFormatException e){
                    error.setText("Only integers");
                    return false;
                }

                error.setText("");
                return true;
            },
            moneyInput.textProperty()
        );

        isBet.addListener(e -> betButton.setDisable(!isBet.get()));
        dialog.getDialogPane().setContent(vBox);
        dialog.setResultConverter(dialogButton -> Integer.parseInt(moneyInput.getText()));
        return dialog.showAndWait().orElseThrow();
    }

    private Call getCall() {
        Dialog<Call> dialog = new Dialog<>();
        ButtonType callButtonType = new ButtonType("Call", ButtonBar.ButtonData.APPLY);
        dialog.getDialogPane().getButtonTypes().setAll(callButtonType);

        TextField moneyInput = new TextField();
        Label error = new Label();
        error.setTextFill(Color.RED);
        ComboBox<Call> callSelector = new ComboBox<>();
        callSelector.getItems().setAll(Call.values());
        callSelector.setPromptText("Calls");
        VBox vBox = new VBox();
        vBox.getChildren().setAll(error, callSelector);
        Node callButton = dialog.getDialogPane().lookupButton(callButtonType);
        callButton.setDisable(true);

        BooleanBinding isCall = Bindings.createBooleanBinding(
            () -> {
                if (callSelector.getValue() == null) {
                    error.setText("Please Select a Call");
                    return false;
                }

                error.setText("");
                return true;
            },
            moneyInput.textProperty()
        );

        isCall.addListener(e -> callButton.setDisable(!isCall.get()));
        dialog.getDialogPane().setContent(vBox);
        dialog.setResultConverter(dialogButton -> callSelector.getValue());

        return dialog.showAndWait().orElseThrow();
    }

    public void drawCard(ActionEvent actionEvent) {

        Socket socket = getInstance().getSocket();

        String output = getServerResponse(socket, "hit", "");

        getCard(output);
        continueGame();
    }

    public void doubleBet(ActionEvent actionEvent) {
        Socket socket = getInstance().getSocket();

        String output = getServerResponse(socket, "doubledown", "");
        // finished
        getCard(output);
        waitTillRoundEnd();
    }

    public void holdBet(ActionEvent actionEvent) {
        // stay

        Socket socket = getInstance().getSocket();

        String output = getServerResponse(socket, "stay", "");
        // finishes round
        continueGame();
    }

    public void goHome(ActionEvent actionEvent) {
        HelloApplication.setStageTo("home.fxml");
    }

    public void getPlayers(Socket socket){
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(
                    new MessageContent("getplayers", "")
            );

            // prepare printStream
            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
            // send json
            printStream.println(jsonString);
            // prepare reader
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader
            String output = clientReader.readLine();

            MessageContent messageContent = unwrapContent(output, MessageContent.class);

            String content = messageContent.content();

            players = unwrapContent(content, PlayerContent[].class);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String getServerResponse(Socket socket, String method, String content){

        String output = "error";

        try {
            // Prepare json
            String jsonString = wrapObject(
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

        Socket socket = getInstance().getSocket();
        String myName = getInstance().getUserName();

        MessageContent messageContent = unwrapContent(output, MessageContent.class);

        String content = messageContent.content();

        Card extraCard = unwrapContent(content, Card.class);

        Player myPlayer = listOfPlayers.stream().filter(
                player -> player.getName().equals(myName)).findFirst().get();

        myPlayer.addCard(extraCard);
    }

    public void continueGame(){

        Socket socket = getInstance().getSocket();

        try{
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String output = clientReader.readLine();

            MessageContent messageContent = unwrapContent(output, MessageContent.class);

            if(messageContent.method().equals("out")) {
                // disable buttons
                waitTillRoundEnd();
            }
            else {
                // enable buttons
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public void waitTillRoundEnd() {
        Socket socket = getInstance().getSocket();
        BufferedReader clientReader = null;
        try {
            clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (clientReader == null) {
            return;
        }

        MessageContent messageContent = null;

        do {
            try {
                messageContent = unwrapContent(clientReader.readLine(), MessageContent.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (messageContent == null) {
                return;
            }

            switch (messageContent.method()) {
                case "hit", "doubleDown" -> {
                    PlayerCardContent playerCardContent =
                            unwrapContent(messageContent.content(), PlayerCardContent.class); // one card

                    listOfPlayers.stream().filter(
                            player -> player.getName().equals(playerCardContent.name())
                    ).findFirst().orElseThrow().getCards().add(playerCardContent.cards()[0]);

                    updatePlayersFxml();
                }
                case "dealerAdd" -> {
                    CardContent cardContent = unwrapContent(messageContent.content(), CardContent.class);

                    dealerCards.add(cardContent.card());

                    updateDealersFxml();
                }
                case "lost" -> {
                    // TODO: show lost

                }
                case "blackjack" -> {
                    // TODO: show blackjack
                    money += betAmount * 5 / 2;

                }
                case "draw" -> {
                    // TODO: show draw
                    money += betAmount;
                }
                default -> {
                }
            }
        } while (!messageContent.method().equalsIgnoreCase("take"));

        listOfPlayers = null;
        
        if (isEnd(clientReader)) {
            // TODO: END GAME
            // TODO: FILL LEADERBOARD
        } else {
            startRound();
        }
    }
    
    private boolean isEnd(BufferedReader clientReader) {
        try {
            MessageContent messageContent = unwrapContent(clientReader.readLine(), MessageContent.class);
            if (messageContent.method().equalsIgnoreCase("finish")) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public <T> T unwrapContent(String content, Class<T> contentClass) {
        ObjectMapper mapper = new ObjectMapper();
        T value = null;

        try {
            value = mapper.readValue(content, contentClass);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return value;
    }

    public String wrapObject(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
