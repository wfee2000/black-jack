package at.htlleonding.frontend.SessionHandler;

import java.io.IOException;
import java.net.Socket;

public class SessionHandler {

    private static SessionHandler instance;

    private Socket socket;

    private String userName;
    private int currentRoom;

    private SessionHandler() {
        try {
            socket = new Socket("localhost", 5000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SessionHandler getInstance() {
        if (instance == null) {
            instance = new SessionHandler();
        }
        return instance;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return this.userName;
    }

    public int getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(int currentRoom) {
        this.currentRoom = currentRoom;
    }
}
