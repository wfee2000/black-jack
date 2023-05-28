package at.htlleonding.frontend.SocketHandler;

import java.io.IOException;
import java.net.Socket;

public class SocketHandler {

    private static SocketHandler instance;

    private Socket socket;

    private SocketHandler() {
        try {
            socket = new Socket("localhost", 5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SocketHandler getInstance() {
        if (instance == null) {
            instance = new SocketHandler();
        }
        return instance;
    }

    public Socket getSocket() {
        return socket;
    }

}
