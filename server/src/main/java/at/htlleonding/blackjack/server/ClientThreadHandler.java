package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.LoginContent;
import at.htlleonding.blackjack.server.contents.MessageContent;
import at.htlleonding.blackjack.server.database.models.PlayerModel;
import at.htlleonding.blackjack.server.database.repositories.GlobalLeaderboardRepository;
import at.htlleonding.blackjack.server.database.repositories.PlayerRepository;
import at.htlleonding.blackjack.server.game.Call;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class ClientThreadHandler extends Thread {
    private final Socket client;
    private boolean isLoggedIn;

    private boolean isInGame;

    private static final ObjectMapper mapper = new ObjectMapper();
    private PrintWriter clientOut;
    private Call call;
    private String name;

    public ClientThreadHandler(Socket client) {
        this.client = client;
        this.isLoggedIn = false;
    }

    @Override
    public void run() {
        try (BufferedReader clientMessagesIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter clientMessagesOut = new PrintWriter(client.getOutputStream(), true)) {
            clientOut = clientMessagesOut;
            while (true) {
                if (!processClientInteraction(mapper.readValue(clientMessagesIn.readLine(), MessageContent.class),
                        clientMessagesOut)) {

                    if(isLoggedIn){
                        client.close();
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean processClientInteraction(MessageContent clientMessage, PrintWriter clientMessagesOut) {
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

        if (isInGame && Arrays.stream(Call.values()).anyMatch(call -> call.toString().equalsIgnoreCase(command))) {
            call = Call.valueOf(command);
            notify();
        }

        switch (command.toLowerCase()) {
            case "globalleaderboard" -> {

                String entries = getGlobalLeaderboard();

                clientMessagesOut.println(entries);

                return true;
            }
            case "quit" -> {
                return false;
            }
        }

        return false;
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

    public static String getGlobalLeaderboard(){

        ObjectMapper mapper = new ObjectMapper();

        List entries = GlobalLeaderboardRepository.getAllEntries();

        String jsonString = "";

        try {
            jsonString = mapper.writeValueAsString(new MessageContent("getGlobalLeaderboard",
                    mapper.writeValueAsString(
                            entries
                    )));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return jsonString;
    }

    public Call requireCall() {
        try {
            String request = mapper.writeValueAsString(new MessageContent("call", ""));
            clientOut.println(request);
            wait();
            return call;
        } catch (InterruptedException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
