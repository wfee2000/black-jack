package at.htlleonding.blackjack.server.database.repositories;

import at.htlleonding.blackjack.server.database.models.EntryModel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GlobalLeaderboardRepository {

    public static EntryModel getEntry(String name, String points) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db")) {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM LEADERBOARD WHERE user_name = ? and user_points = ?");

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, Integer.parseInt(points));

            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                return new EntryModel(name, points);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public static List<EntryModel> getAllEntries() {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db")) {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM LEADERBOARD ORDER BY user_points");

            ResultSet resultSet = preparedStatement.executeQuery();

            List<EntryModel> allEntries = new ArrayList<>();

            while(resultSet.next()) {
                 allEntries.add(new EntryModel(
                         resultSet.getString("USER_NAME"),
                         String.valueOf(resultSet.getInt("USER_POINTS"))
                 ));
            }

            return allEntries;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean addEntry(String name, int points) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db")) {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO LEADERBOARD (user_name, user_points) VALUES (?, ?)");

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, points);

            preparedStatement.executeUpdate();

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean doesEntryExists(String name, int points) {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db")) {

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT count(*) FROM LEADERBOARD WHERE user_name = ? and user_points = ?");

            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, points);

            ResultSet result = preparedStatement.executeQuery();

            if (result.next()) {
                return result.getInt(1) == 1;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
