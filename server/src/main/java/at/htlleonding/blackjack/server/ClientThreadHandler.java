package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.LoginContent;
import at.htlleonding.blackjack.server.models.PlayerModel;
import at.htlleonding.blackjack.server.repositories.PlayerRepository;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ClientThreadHandler extends Thread {
    private final Socket client;
    private boolean isLoggedIn;
    private String name;

    public ClientThreadHandler(Socket client) {
        this.client = client;
        this.isLoggedIn = false;
    }

    @Override
    public void run() {
        try (BufferedReader clientMessagesIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
             PrintWriter clientMessagesOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()))) {
            while (true) {
                final JSONObject clientMessageWrapper = new JSONObject(clientMessagesIn.readLine());
                final String command = (String) clientMessageWrapper.get("command");

                if (!isLoggedIn) {
                    switch (command) {
                        case "login" -> {
                            if (login(clientMessageWrapper)) {
                                isLoggedIn = true;
                            } else {
                                client.close();
                                return;
                            }
                        }
                        case "register" -> {
                            if (register(clientMessageWrapper)) {
                                isLoggedIn = true;
                            } else {
                                client.close();
                                return;
                            }
                        }
                    }
                } else {
                    switch (command) {
                        case "quit":
                            client.close();
                            return;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean login(JSONObject messageWrapper) {
        Object content = messageWrapper.opt("content");

        if (!(content instanceof LoginContent loginContent)) {
            return false;
        }

        PlayerModel player = PlayerRepository.getPlayerWithName(loginContent.name());

        if (player == null) {
            return false;
        }

        return player.password().equals(loginContent.passwordHash());
    }

    public static boolean register(JSONObject messageWrapper) {
        Object content = messageWrapper.opt("content");

        if (!(content instanceof LoginContent loginContent) || PlayerRepository.doesPlayerExist(loginContent.name())) {
            return false;
        }

        PlayerRepository.addPlayer(loginContent.name(), loginContent.passwordHash());
        return true;
    }

    public static boolean deleteAccount(JSONObject messageWrapper) {
        Object content = messageWrapper.opt("content");

        if (!(content instanceof LoginContent loginContent)) {
            return false;
        }

        PlayerModel player = PlayerRepository.getPlayerWithName(loginContent.name());

        if (player == null || !loginContent.passwordHash().equals(player.password())) {
            return false;
        }

        PlayerRepository.deletePlayer(loginContent.name());
        return true;
    }
}
