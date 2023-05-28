package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.LoginContent;
import at.htlleonding.blackjack.server.contents.MessageContent;
import at.htlleonding.blackjack.server.database.models.PlayerModel;
import at.htlleonding.blackjack.server.database.repositories.PlayerRepository;
import at.htlleonding.blackjack.server.game.Call;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;

public class ClientThreadHandler extends Thread {
    private final Socket client;
    private boolean isLoggedIn;

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
                if (!processClientInteraction(mapper.readValue(clientMessagesIn.readLine(), MessageContent.class), clientMessagesOut)) {
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean processClientInteraction(MessageContent clientMessageWrapper, PrintWriter clientMessagesOut) {
        final String command = clientMessageWrapper.method();

        if (!isLoggedIn) {
            switch (command.toLowerCase()) {
                case "login" -> {
                    name = login(clientMessageWrapper);
                    if (name != null) {
                        isLoggedIn = true;
                        clientMessagesOut.println("Connected");
                        return true;
                    }

                    clientMessagesOut.println("Failure");
                    return false;
                }
                case "register" -> {
                    name = register(clientMessageWrapper);
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

        switch (command.toLowerCase()) {
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

    public Call requireCall() {
        try {
            String request = mapper.writeValueAsString(new MessageContent("call", null));
            clientOut.println(request);
            wait();
            return call;
        } catch (InterruptedException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}