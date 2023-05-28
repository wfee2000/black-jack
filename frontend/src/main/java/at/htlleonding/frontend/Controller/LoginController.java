package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.SocketHandler.SocketHandler;
import at.htlleonding.frontend.model.LoginContent;
import at.htlleonding.frontend.model.MessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Objects;


public class LoginController {
    @FXML
    public GridPane content;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private TextField nameField;
    @FXML
    private TextField pwdField;

    @FXML
    public void initialize() {
        System.out.println("LoginController.initialize()");
    }

    public void loginUser(ActionEvent actionEvent) {
        // Socket schicken

        String name = nameField.getText();
        String password;
        Socket socket = SocketHandler.getInstance().getSocket();

        try {
            byte[] hashedPassword = MessageDigest.getInstance("SHA-256").digest(pwdField.getText()
                    .getBytes(StandardCharsets.UTF_8));
            password = new String(Base64.getEncoder().encode(hashedPassword));

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(new MessageContent("login",
                    mapper.writeValueAsString(new LoginContent(name, password))));

            // prepare printStream
            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
            // send json
            printStream.println(jsonString);
            // prepare reader
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader
            String output = clientReader.readLine();
            // output returning message
            System.out.println(output);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void registerUser(ActionEvent actionEvent) {
        try {
            Parent window = FXMLLoader.load(Objects.requireNonNull(this.getClass()
                    .getResource("/at/htlleonding/frontend/register.fxml")));

            Scene newScene = new Scene(window);
            Stage mainWindow = (Stage) content.getScene().getWindow();
            mainWindow.setScene(newScene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}