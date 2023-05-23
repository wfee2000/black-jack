package at.htlleonding.blackjack.server;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.sql.DriverManager;

public class DatabaseTest {
    @Test
    public void test_Database_Connection() {
        Assertions.assertThatCode(() -> DriverManager.getConnection("jdbc:derby:db")).doesNotThrowAnyException();
    }
}
