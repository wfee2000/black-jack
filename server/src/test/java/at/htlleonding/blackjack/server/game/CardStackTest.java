package at.htlleonding.blackjack.server.game;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class CardStackTest {
    @Test
    public void test_Constructor_RemoveFromEmptyStack_ThrowsException() {
        CardStack cardStack = new CardStack(false);

        Assertions.assertThatThrownBy(cardStack::takeCard).isInstanceOf(EmptyStackException.class);
    }

    @Test
    public void test_Constructor_RemoveFromFullStack_RandomCard() {
        CardStack cardStack = new CardStack(true);

        Assertions.assertThat(cardStack.takeCard()).isInstanceOf(Card.class);
    }

    @Test
    public void test_TakeCard_RemoveAllCardsAndOne_ThrowsError() {
        CardStack cardStack = new CardStack(true);

        for (int i = 0; i < 6 * Sign.values().length * Value.values().length; i++) {
            cardStack.takeCard();
        }

        Assertions.assertThatThrownBy(cardStack::takeCard).isInstanceOf(EmptyStackException.class);
    }

    @Test
    public void test_TakeCards_RemoveTwoCards_TwoCards() {
        CardStack cardStack = new CardStack(true);

        Assertions.assertThat(cardStack.takeCards(2)).hasSize(2);
    }

    @Test
    public void test_TakeCards_RemoveAllCards_WholeStack() {
        CardStack cardStack = new CardStack(true);

        Assertions.assertThat(cardStack.takeCards(6 * Sign.values().length * Value.values().length))
                .hasSize(6 * Sign.values().length * Value.values().length);
    }

    @Test
    public void test_TakeCards_RemoveAllCardsAndOne_ThrowsException() {
        CardStack cardStack = new CardStack(true);

        Assertions.assertThatThrownBy(() -> cardStack.takeCards(6 * Sign.values().length * Value.values().length + 1))
                .isInstanceOf(EmptyStackException.class);
    }

    @Test
    public void test_PutCardsBack_RemoveAllCardsAndPutThemBackAndTakeOne_RandomCard() {
        CardStack cardStack = new CardStack(true);

        List<Card> cards = cardStack.takeCards(6 * Sign.values().length * Value.values().length);
        cardStack.putCardsBack(cards);

        Assertions.assertThatCode(cardStack::takeCard).isNotNull().doesNotThrowAnyException();
    }
}
