package at.htlleonding.frontend.Controller;

import at.htlleonding.frontend.HelloApplication;
import at.htlleonding.frontend.SessionHandler.SessionHandler;
import at.htlleonding.frontend.model.LoginContent;
import at.htlleonding.frontend.model.MessageContent;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
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

public class RegisterController {
    @FXML
    public GridPane content;
    @FXML
    private TextField nameField;
    @FXML
    private TextField pwdField;

    @FXML
    public void initialize() {
    }

    public void registerUser(ActionEvent actionEvent) {

        String name = nameField.getText();
        String password = pwdField.getText();

        Socket socket = SessionHandler.getInstance().getSocket();

        if(name.isEmpty() || password.isEmpty() ||
                name.chars().allMatch(c -> c == (int)' ') || password.chars().allMatch(c -> c == (int)' ')){

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setContentText("Empty username or password!");

            alert.showAndWait();

            return;
        }

        try {

            byte[] hashedPassword = MessageDigest.getInstance("SHA-256").digest(pwdField.getText()
                    .getBytes(StandardCharsets.UTF_8));
            password = new String(Base64.getEncoder().encode(hashedPassword));

            ObjectMapper mapper = new ObjectMapper();
            // Prepare json
            String jsonString = mapper.writeValueAsString(new MessageContent("register",
                    mapper.writeValueAsString(new LoginContent(name, password))));

            // prepare printStream
            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);
            // send json
            printStream.println(jsonString);
            // prepare reader
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // wait for reader
            String output = clientReader.readLine();

            if(output != null && output.equals("Connected")){
                try {

                    SessionHandler.getInstance().setUserName(name);

                    HelloApplication.setStageTo("home.fxml");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setContentText("User already exists!");

                alert.showAndWait();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
