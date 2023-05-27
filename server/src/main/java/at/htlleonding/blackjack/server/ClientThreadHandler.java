package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.LoginContent;
import at.htlleonding.blackjack.server.database.models.PlayerModel;
import at.htlleonding.blackjack.server.database.repositories.PlayerRepository;
import at.htlleonding.blackjack.server.game.Call;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class ClientThreadHandler extends Thread {
    private final Socket client;
    private boolean isLoggedIn;

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
             PrintWriter clientMessagesOut = new PrintWriter(new OutputStreamWriter(client.getOutputStream()))) {
            clientOut = clientMessagesOut;
            while (true) {
                if (!processClientInteraction(new JSONObject(clientMessagesIn.readLine()), clientMessagesOut)) {
                    client.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean processClientInteraction(JSONObject clientMessageWrapper, PrintWriter clientMessagesOut) {
        final String command = (String) clientMessageWrapper.get("command");

        if (!isLoggedIn) {
            switch (command.toLowerCase()) {
                case "login" -> {
                    name = login(clientMessageWrapper);
                    if (name != null) {
                        isLoggedIn = true;
                        return true;
                    }

                    return false;
                }
                case "register" -> {
                    name = register(clientMessageWrapper);
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

    public static String login(JSONObject messageWrapper) {
        Object content = messageWrapper.opt("content");

        if (!(content instanceof LoginContent loginContent)) {
            return null;
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

    public static String register(JSONObject messageWrapper) {
        Object content = messageWrapper.opt("content");

        if (!(content instanceof LoginContent loginContent) || PlayerRepository.doesPlayerExist(loginContent.name())) {
            return null;
        }

        PlayerRepository.addPlayer(loginContent.name(), loginContent.passwordHash());
        return loginContent.name();
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

    public Call requireCall() {
        try {
            JSONObject request = new JSONObject();
            request.put("method", "get");
            request.put("value", "call");

            clientOut.println(request);
            wait();
            return call;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
