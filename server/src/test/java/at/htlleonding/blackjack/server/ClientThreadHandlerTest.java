package at.htlleonding.blackjack.server;

import at.htlleonding.blackjack.server.contents.LoginContent;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.db.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientThreadHandlerTest {
    @Test
    @Order(1)
    public void test_Login_LoginPlayerThatDoesntExists_False() {
        Table table = new Table(new Source("jdbc:derby:db", "", ""), "Player");
        JSONObject contentWrapper = new JSONObject();

        contentWrapper.put("content", new LoginContent("test_account", "1234"));

        assertThat(ClientThreadHandler.login(contentWrapper)).isNull();
        assertThat(table).isEmpty();
    }

    @Test
    @Order(2)
    public void test_Delete_DeleteNonExistentPlayer_False() {
        Table table = new Table(new Source("jdbc:derby:db", "", ""), "Player");
        JSONObject contentWrapper = new JSONObject();

        contentWrapper.put("content", new LoginContent("test_account", "1234"));

        assertThat(table).isEmpty();
        assertThat(ClientThreadHandler.deleteAccount(contentWrapper)).isFalse();
        assertThat(table).isEmpty();
    }

    @Test
    @Order(3)
    public void test_Create_AddPlayer_True() {
        Table table = new Table(new Source("jdbc:derby:db", "", ""), "Player");
        JSONObject contentWrapper = new JSONObject();
        String name = "test_account";
        String password = "1234";

        contentWrapper.put("content", new LoginContent(name, password));

        assertThat(ClientThreadHandler.register(contentWrapper)).isEqualTo(name);
        assertThat(table)
                .column("user_name").value().isEqualTo(name)
                .column("user_pwd").value().isEqualTo(password);
    }

    @Test
    @Order(4)
    public void test_Create_AddPlayerAgain_False() {
        Table table = new Table(new Source("jdbc:derby:db", "", ""), "Player");
        JSONObject contentWrapper = new JSONObject();
        String name = "test_account";
        String password = "1234";
        String wrongPassword = "12345";

        contentWrapper.put("content", new LoginContent(name, wrongPassword));

        assertThat(ClientThreadHandler.register(contentWrapper)).isNull();
        assertThat(table)
                .column("user_name").value().isEqualTo(name)
                .column("user_pwd").value().isEqualTo(password);
    }

    @Test
    @Order(5)
    public void test_Login_LoginPlayerThatExists_True() {
        Table table = new Table(new Source("jdbc:derby:db", "", ""), "Player");
        JSONObject contentWrapper = new JSONObject();
        String name = "test_account";
        String password = "1234";

        contentWrapper.put("content", new LoginContent(name, password));

        assertThat(ClientThreadHandler.login(contentWrapper)).isEqualTo(name);
        assertThat(table)
                .column("user_name").value().isEqualTo(name);
    }

    @Test
    @Order(6)
    public void test_Delete_DeleteExistingPlayer_True() {
        Table table = new Table(new Source("jdbc:derby:db", "", ""), "Player");
        JSONObject contentWrapper = new JSONObject();

        contentWrapper.put("content", new LoginContent("test_account", "1234"));

        assertThat(ClientThreadHandler.deleteAccount(contentWrapper)).isTrue();
        assertThat(table).isEmpty();
    }
}
