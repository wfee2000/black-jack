package at.htlleonding.blackjack.server.game;

import at.htlleonding.blackjack.server.ClientThreadHandler;
import at.htlleonding.blackjack.server.contents.CardContent;
import at.htlleonding.blackjack.server.contents.MessageContent;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private int coins;
    private int bet;
    public int getBet() {
        return bet;
    }
    public boolean isOut() {
        return isOut;
    }
    public void isOut(boolean isOut) {
        this.isOut = isOut;
    }
    private boolean isOut;
    public boolean hasBlackJack() {
        return hasBlackJack;
    }
    public void hasBlackJack(boolean hasBlackJack) {
        this.hasBlackJack = hasBlackJack;
    }
    private boolean hasBlackJack;


    private final List<Card> cards;

    private final ClientThreadHandler client;

    public Player(ClientThreadHandler client) {
        this.client = client;
        cards = new ArrayList<>();
        coins = 100;
    }

    public boolean doubleDown(Card card) {
        if (coins < bet) {
            return false;
        }

        hit(card);
        coins -= bet;
        bet *= 2;
        isOut = true;
        return true;
    }

    public void hit(Card card) {
        cards.add(card);
        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("add",
                    ClientThreadHandler.mapper.writeValueAsString(new CardContent(card)))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean distribute(List<Card> cards) {
        this.cards.addAll(cards);
        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("distributed",
                    ClientThreadHandler.mapper.writeValueAsString(cards.stream().map(CardContent::new).toArray()))));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return Card.getSum(cards) == 21;
    }

    public void stay() {
        isOut = true;
    }

    public void triggerLoose() {
        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("lost","")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        bet = 0;
    }

    public void triggerWin() {
        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("lost","")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        coins += bet * 2;
        bet = 0;
    }

    public void triggerBlackjack() {
        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("blackjack","")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        coins += bet * 3 / 2;
        bet = 0;
    }

    public void triggerDraw() {
        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("draw","")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        coins += bet;
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

        try {
            client.sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("take","")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return cards;
    }

    public ClientThreadHandler getClient() {
        return client;
    }

    public void surrender() {
        coins += bet / 2;
        bet = 0;
    }

    public List<Card> getCards() {
        return cards;
    }
}
