package at.htlleonding.blackjack.server.database;

import org.apache.ibatis.jdbc.ScriptRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class Database {
    public void createDB() {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db;create=true")) {
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.runScript(new BufferedReader(
                    new FileReader(Objects.requireNonNull(
                            getClass().getResource("/sql/createTables.sql")).getFile())));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void dropDB() {
        try (Connection connection = DriverManager.getConnection("jdbc:derby:db;create=true")) {
            ScriptRunner scriptRunner = new ScriptRunner(connection);
            scriptRunner.runScript(new BufferedReader(
                    new FileReader(Objects.requireNonNull(
                            getClass().getResource("/sql/deleteTables.sql")).getFile())));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
