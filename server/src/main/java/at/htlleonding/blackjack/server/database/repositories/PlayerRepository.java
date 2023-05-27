package at.htlleonding.blackjack.server.database.repositories;

import at.htlleonding.blackjack.server.database.models.PlayerModel;

import java.sql.*;

public class PlayerRepository {
    public static PlayerModel getPlayerWithName(String name) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db");
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYER WHERE user_name = ?")) {
            preparedStatement.setString(1, name);
            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                String hashedPwd = result.getString("user_pwd");
                return new PlayerModel(name, hashedPwd);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addPlayer(String name, String passwordHash) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db");
             PreparedStatement preparedStatement =
                     connection.prepareStatement("INSERT INTO PLAYER (user_name, user_pwd) VALUES (?, ?)")) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, passwordHash);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean doesPlayerExist(String name) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db");
             PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT COUNT(*) FROM PLAYER WHERE user_name = ?")) {
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void deletePlayer(String name) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db");
             PreparedStatement preparedStatement =
                     connection.prepareStatement("DELETE FROM PLAYER WHERE user_name = ?")) {
            preparedStatement.setString(1, name);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
