package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.MessageContent;

import java.io.IOException;
import java.io.PrintWriter;

public class ClientInteractionHandler extends Thread {
    private final ClientThreadHandler threadHandler;
    private final MessageContent clientMessage;
    private final PrintWriter clientMessagesOut;

    public ClientInteractionHandler(ClientThreadHandler clientThreadHandler, MessageContent clientMessage,
                                    PrintWriter clientMessagesOut) {
        this.threadHandler = clientThreadHandler;
        this.clientMessage = clientMessage;
        this.clientMessagesOut = clientMessagesOut;
    }
    @Override
    public void run() {
        try {
            value = threadHandler.processClientInteraction(clientMessage, clientMessagesOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean value;
}
