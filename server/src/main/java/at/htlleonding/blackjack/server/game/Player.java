package at.htlleonding.blackjack.server.game;

import at.htlleonding.blackjack.server.ClientThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int coins;

    private int bet;

    private boolean isOut;

    private final List<Card> cards;

    private final ClientThreadHandler client;

    public Player(ClientThreadHandler client) {
        this.client = client;
        cards = new ArrayList<>();
        coins = 100;
    }

    public Card doubleDown(Card card) {
        if (coins < bet) {
            return  null;
        }

        coins -= bet;
        bet *= 2;

        return hit(card);
    }

    public Card hit(Card card) {
        if (isOut) {
            return null;
        }

        cards.add(card);
        isOut = Card.getSum(cards) > 21;
        triggerLoose();
        return card;
    }

    public boolean distribute(List<Card> cards) {
        this.cards.addAll(cards);
        if (Card.getSum(cards) == 21) {
            return isOut = true;
        }

        return false;
    }

    public void stay() {
        isOut = true;
    }

    public void triggerLoose() {
        bet = 0;
    }

    public void triggerWin() {
        coins += bet * 2;
        bet = 0;
    }

    public void triggerBlackjack() {
        coins += bet * 3 / 2;
        bet = 0;
    }

    public boolean setBet(int bet) {
        if (coins < bet) {
            return false;
        }

        coins -= bet;
        this.bet = bet;
        return true;
    }

    public List<Card> takeCardsAway() {
        List<Card> cards = this.cards;
        this.cards.clear();
        return cards;
    }

    public ClientThreadHandler getClient() {
        return client;
    }
}
