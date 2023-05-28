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

    private final ObjectMapper mapper;

    private PrintWriter clientOut;
    private Call call;
    private String name;

    public ClientThreadHandler(Socket client) {
        this.client = client;
        this.isLoggedIn = false;
        this.mapper = new ObjectMapper();
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

    public boolean processClientInteraction(MessageContent clientMessage, PrintWriter clientMessagesOut) {
        final String command = clientMessage.method();

        if (!isLoggedIn) {
            switch (command.toLowerCase()) {
                case "login" -> {
                    name = login(clientMessage);
                    if (name != null) {
                        isLoggedIn = true;
                        return true;
                    }

                    clientMessagesOut.println("Login failed");

                    return false;
                }
                case "register" -> {
                    name = register(clientMessage);
                    if (name != null) {
                        isLoggedIn = true;
                        return true;
                    }

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

    public String login(MessageContent message) {
        LoginContent content;

        try {
            content = mapper.readValue(message.content(), LoginContent.class);
        } catch (JsonProcessingException e) {
            return null;
        }

        PlayerModel player = PlayerRepository.getPlayerWithName(content.name());

        if (player == null) {
            return null;
        }

        if (player.password().equals(content.passwordHash())) {
            return player.name();
        }

        return null;
    }

    public String register(MessageContent message) {
        LoginContent content;

        try {
            content = mapper.readValue(message.content(), LoginContent.class);
        } catch (JsonProcessingException e) {
            return null;
        }

        PlayerRepository.addPlayer(content.name(), content.passwordHash());
        return content.name();
    }

    public boolean deleteAccount(MessageContent message) {
        LoginContent content;

        try {
            content = mapper.readValue(message.content(), LoginContent.class);
        } catch (JsonProcessingException e) {
            return false;
        }

        PlayerModel player = PlayerRepository.getPlayerWithName(content.name());

        if (player == null || !content.passwordHash().equals(player.password())) {
            return false;
        }

        PlayerRepository.deletePlayer(content.name());
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
