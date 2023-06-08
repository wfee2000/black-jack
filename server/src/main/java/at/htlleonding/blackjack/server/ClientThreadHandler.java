package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.*;
import at.htlleonding.blackjack.server.database.models.EntryModel;
import at.htlleonding.blackjack.server.database.models.PlayerModel;
import at.htlleonding.blackjack.server.database.repositories.GlobalLeaderboardRepository;
import at.htlleonding.blackjack.server.database.repositories.PlayerRepository;
import at.htlleonding.blackjack.server.game.Call;
import at.htlleonding.blackjack.server.game.Dealer;
import at.htlleonding.blackjack.server.repository.RoomRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ClientThreadHandler extends Thread {
    public static final ObjectMapper mapper = new ObjectMapper();
    private final Socket client;
    private boolean isLoggedIn;
    private Dealer currentGame;
    private PrintWriter clientOut;
    private Call call;
    private String name;
    private boolean waitingForCall;
    private int bet;
    private boolean waitingForBet;

    public ClientThreadHandler(Socket client) {
        this.client = client;
        this.isLoggedIn = false;
    }

    public static String login(MessageContent messageWrapper) {
        String content = messageWrapper.content();
        LoginContent loginContent;

        try {
            loginContent = mapper.readValue(content, LoginContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        PlayerModel player = PlayerRepository.getPlayerWithName(loginContent.name());

        if (player == null) {
            return null;
        }

        if (player.password().equals(loginContent.passwordHash())) {
            return player.name();
        }

        return null;
    }

    public static String register(MessageContent messageWrapper) {
        String content = messageWrapper.content();
        LoginContent loginContent;

        try {
            loginContent = mapper.readValue(content, LoginContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (PlayerRepository.doesPlayerExist(loginContent.name())) {
            return null;
        }

        PlayerRepository.addPlayer(loginContent.name(), loginContent.passwordHash());
        return loginContent.name();
    }

    public static boolean deleteAccount(MessageContent messageWrapper) {
        String content = messageWrapper.content();
        LoginContent loginContent;

        try {
            loginContent = mapper.readValue(content, LoginContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        PlayerModel player = PlayerRepository.getPlayerWithName(loginContent.name());

        if (player == null || !loginContent.passwordHash().equals(player.password())) {
            return false;
        }

        PlayerRepository.deletePlayer(loginContent.name());
        return true;
    }

    public static String getGlobalLeaderboard() {

        ObjectMapper mapper = new ObjectMapper();

        List<EntryModel> entries = GlobalLeaderboardRepository.getAllEntries();

        String jsonString;

        try {
            jsonString = mapper.writeValueAsString(
                    new MessageContent("getGlobalLeaderboard", mapper.writeValueAsString(entries)));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public static String getRooms() {
        RoomContent[] rooms = (RoomContent[]) RoomRepository.getInstance().getRooms().stream().filter(room -> room.getPassword().isEmpty())
                .map(room -> new RoomContent(room.getPlayerCount(), room.getMaxPlayers(), room.getId()))
                .toArray();

        String jsonString;

        try {
            jsonString = mapper.writeValueAsString(new MessageContent("rooms",
                    mapper.writeValueAsString(rooms)));
        } catch (Exception ignored) {
            return "";
        }

        return jsonString;
    }

    public static Dealer join(MessageContent messageWrapper) {
        String content = messageWrapper.content();
        JoinContent joinContent;

        try {
            joinContent = mapper.readValue(content, JoinContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Dealer dealer = RoomRepository.getInstance().getRoom(joinContent.id());

        if (dealer == null || dealer.getPlayerCount() == dealer.getMaxPlayers()) {
            return null;
        }

        return dealer;
    }

    public static Dealer create(MessageContent messageWrapper) {
        String content = messageWrapper.content();
        CreateContent createContent;

        try {
            createContent = mapper.readValue(content, CreateContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        Dealer dealer = RoomRepository.getInstance().addRoom(createContent.maxPlayers(), createContent.rounds(),
                createContent.name(), createContent.password());

        if (dealer == null || dealer.getPlayerCount() == dealer.getMaxPlayers()) {
            return null;
        }

        return dealer;
    }

    private int getBet(MessageContent messageWrapper) {
        String content = messageWrapper.content();
        BetContent betContent;

        try {
            betContent = mapper.readValue(content, BetContent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (betContent.bet() < 0) {
            return -1;
        }

        return betContent.bet();
    }

    @Override
    public void run() {
        try (BufferedReader clientMessagesIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter clientMessagesOut = new PrintWriter(client.getOutputStream(), true)) {
            clientOut = clientMessagesOut;
            while (true) {
                if (!processClientInteraction(mapper.readValue(clientMessagesIn.readLine(), MessageContent.class),
                        clientMessagesOut)) {
                    if (client.isConnected()) {
                        clientMessagesOut.println("Something went wrong");
                    } else {
                        return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean processClientInteraction(@NotNull MessageContent clientMessage, PrintWriter clientMessagesOut) throws IOException {
        final String command = clientMessage.method();

        if (!isLoggedIn) {
            switch (command.toLowerCase()) {
                case "login" -> {
                    name = login(clientMessage);
                    if (name != null) {
                        isLoggedIn = true;
                        clientMessagesOut.println("Connected");
                        return true;
                    }

                    clientMessagesOut.println("Failure");
                    return false;
                }
                case "register" -> {
                    name = register(clientMessage);
                    if (name != null) {
                        isLoggedIn = true;
                        clientMessagesOut.println("Connected");
                        return true;
                    }

                    clientMessagesOut.println("Failure");
                    return false;
                }
            }

            return false;
        }

        if (currentGame == null) {
            switch (command.toLowerCase()) {
                case "rooms" -> {
                    clientMessagesOut.println(getRooms());
                    return true;
                }
                case "join" -> {
                    Dealer dealer = join(clientMessage);

                    if (dealer != null) {
                        dealer.addPlayer(this);
                        clientMessagesOut.println("Success");
                        currentGame = dealer;
                        return true;
                    }

                    clientMessagesOut.println("Could not connect to this room");
                    return false;
                }
                case "open" -> {
                    Dealer dealer = create(clientMessage);

                    if (dealer != null) {
                        dealer.addPlayer(this);
                        clientMessagesOut.println("Success");
                        currentGame = dealer;
                        return true;
                    }

                    clientMessagesOut.println("Maximum number of possible rooms are created");
                    return false;
                }
                case "quit" -> {
                    clientMessagesOut.println(mapper.writeValueAsString(
                            new MessageContent("closed", "")));
                    client.close();
                    return false;
                }
                case "leaderboard" -> {
                    String entries = getGlobalLeaderboard();
                    clientMessagesOut.println(entries);
                    return true;
                }
            }

            return false;
        }

        if (currentGame.hasStarted()) {
            switch (command) {
                case "bet" -> {
                    if (waitingForBet) {
                        bet = getBet(clientMessage);

                        if (bet == -1) {
                            clientMessagesOut.println("Invalid Bet");
                            return false;
                        }

                        waitingForBet = false;
                        notify();
                        clientMessagesOut.println("Success");
                        return true;
                    }

                    clientMessagesOut.println("No Bet waiting");
                    return false;
                }
                case "leave" -> {
                    if (currentGame.removePlayer(this)) {
                        clientMessagesOut.println("Success");
                        currentGame = null;
                        return true;
                    }

                    clientMessagesOut.println("This is a bug");
                    return false;
                }
                case "start" -> {
                    if (currentGame.start()) {
                        clientMessagesOut.println("Success");
                        return true;
                    }

                    clientMessagesOut.println("Waiting for bets");
                    return false;
                }
            }
        }

        if (waitingForCall && Arrays.stream(Call.values())
                .anyMatch(call -> call.toString().equalsIgnoreCase(command))) {
            call = Call.valueOf(command);
            waitingForCall = false;
            notify();
            clientMessagesOut.println("Success");
            return true;
        }

        clientMessagesOut.println("Nothing found");
        return false;
    }

    public Call requireCall() {
        try {
            String request = mapper.writeValueAsString(new MessageContent("call", ""));
            waitingForCall = true;
            clientOut.println(request);
            wait();
            return call;
        } catch (InterruptedException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(String message) {
        clientOut.println(message);
    }

    public int requireBet() {
        try {
            String request = mapper.writeValueAsString(new MessageContent("bet", ""));
            waitingForBet = true;
            clientOut.println(request);
            wait();
            return bet;
        } catch (InterruptedException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}