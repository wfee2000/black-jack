package at.htlleonding.blackjack.server;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketServer {
    public static void entryPoint() {
        final int port = 5000;

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("SocketServer listening on port: " + port);

            //noinspection InfiniteLoopStatement
            while (true) {
                new ClientThreadHandler(server.accept()).start();
                System.out.println("Client connected");
            }
        } catch (IOException e) {
            System.out.printf("Port %d is currently occupied%n", port);
        }
    }
}
