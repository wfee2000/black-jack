package htlleonding.frontend.Controller;

import htlleonding.frontend.SocketHandler.SocketHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.json.JSONObject;


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

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void loginUser(ActionEvent actionEvent) {
        // Socket schicken

        String name = nameField.getText();
        String pwd;

        Socket socket = SocketHandler.getInstance().getSocket();

        try {

            byte[] hashedpwd = MessageDigest.getInstance("SHA-256").digest(pwdField.getText().getBytes(StandardCharsets.UTF_8));

            pwd = bytesToHex(hashedpwd);

            PrintStream printStream = new PrintStream(socket.getOutputStream(), true);

            JSONObject jsonObject = new JSONObject();

            jsonObject.put("method", "login");

            JSONObject data = new JSONObject();

            data.put("name", name);
            data.put("passwordHash", pwd);

            jsonObject.put("content", data);

            printStream.println(jsonObject);

            InputStream input = socket.getInputStream();

            BufferedReader x = new BufferedReader(new InputStreamReader(input));

            System.out.println(x.readLine());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void registerUser(ActionEvent actionEvent) {
        try {

            Parent window = FXMLLoader.load(this.getClass().getResource("/htlleonding/frontend/register.fxml"));

            Scene newScene = new Scene(window);

            Stage mainWindow = (Stage) content.getScene().getWindow();

            mainWindow.setScene(newScene);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}