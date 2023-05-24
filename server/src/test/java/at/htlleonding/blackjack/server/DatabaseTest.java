package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.database.Database;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;

public class DatabaseTest {
    static Database db;
    @BeforeAll
    public static void ensureDBCreated() {
        db = new Database();
        db.createDB();
    }
    @Test
    public void test_Database_Connection() {
        Assertions.assertThatCode(() -> DriverManager.getConnection("jdbc:derby:db")).doesNotThrowAnyException();
    }
    @AfterAll
    public static void dropDB() {
        db.dropDB();
    }
}
