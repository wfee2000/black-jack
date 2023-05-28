package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.LoginContent;
import at.htlleonding.blackjack.server.contents.MessageContent;
import at.htlleonding.blackjack.server.database.Database;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.db.output.Outputs;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientThreadHandlerTest {
    static Database db;
    static ObjectMapper mapper = new ObjectMapper();
    static Source source = new Source("jdbc:derby:db", "", "");
    @BeforeAll
    public static void ensureDBCreated() {
        db = new Database();
        db.createDB();
    }
    @Test
    @Order(1)
    public void test_Login_LoginPlayerThatDoesntExists_False() {
        Table table = new Table(source, "Player");

        try {
            assertThat(ClientThreadHandler.login(new MessageContent("", mapper.writeValueAsString(
                    new LoginContent("test_account", "1234"))))).isNull();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(table).isEmpty();
    }

    @Test
    @Order(2)
    public void test_Delete_DeleteNonExistentPlayer_False() {
        Table table = new Table(source, "Player");

        assertThat(table).isEmpty();
        try {
            assertThat(ClientThreadHandler.deleteAccount(new MessageContent("", mapper.writeValueAsString(
                    new LoginContent("test_account", "1234"))))).isFalse();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(table).isEmpty();
    }

    @Test
    @Order(3)
    public void test_Create_AddPlayer_True() {
        Table table = new Table(source, "Player");
        String name = "test_account";
        String password = "1234";

        try {
            assertThat(ClientThreadHandler.register(new MessageContent("", mapper.writeValueAsString(
                    new LoginContent(name, password))))).isEqualTo(name);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        Outputs.output(table).toConsole();

        assertThat(table)
                .column("user_name").value().isEqualTo(name)
                .column("user_pwd").value().isEqualTo(password);
        Outputs.output(table).toConsole();
    }

    @Test
    @Order(4)
    public void test_Create_AddPlayerAgain_False() {
        Table table = new Table(source, "Player");
        Outputs.output(table).toConsole();
        String name = "test_account";
        String password = "1234";
        String wrongPassword = "12345";

        try {
            assertThat(ClientThreadHandler.register(new MessageContent("", mapper.writeValueAsString(
                    new LoginContent(name, wrongPassword))))).isNull();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(table)
                .column("user_name").value().isEqualTo(name)
                .column("user_pwd").value().isEqualTo(password);
    }

    @Test
    @Order(5)
    public void test_Login_LoginPlayerThatExists_True() {
        Table table = new Table(source, "Player");
        String name = "test_account";
        String password = "1234";

        try {
            assertThat(ClientThreadHandler.login(new MessageContent("", mapper.writeValueAsString(
                    new LoginContent(name, password))))).isEqualTo(name);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(table)
                .column("user_name").value().isEqualTo(name);
    }

    @Test
    @Order(6)
    public void test_Delete_DeleteExistingPlayer_True() {
        Table table = new Table(source, "Player");

        try {
            assertThat(ClientThreadHandler.deleteAccount(
                    new MessageContent("", mapper.writeValueAsString(
                            new LoginContent("test_account", "1234"))))).isTrue();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        assertThat(table).isEmpty();
    }

    @AfterAll
    public static void dropDB() {
        db.dropDB();
    }
}
