package at.htlleonding.blackjack.server.game;

import at.htlleonding.blackjack.server.ClientThreadHandler;

import java.util.ArrayList;
import java.util.List;

public class Dealer {
    private final List<Player> players;

    private final int rounds;

    private final int maxPlayers;

    private final CardStack cardStackTake;
    private final CardStack cardStackPlace;

    private final List<Card> cards;

    private boolean hasStarted;

    public Dealer(int rounds, int maxPlayers) {
        this.rounds = rounds;
        this.maxPlayers = maxPlayers;
        cards = new ArrayList<>();
        players = new ArrayList<>();
        cardStackTake = new CardStack(true);
        cardStackPlace = new CardStack(false);
    }

    public boolean addPlayer(ClientThreadHandler client) {
        if (hasStarted || maxPlayers == players.size()) {
            return false;
        }

        return players.add(new Player(client));
    }

    public boolean removePlayer(ClientThreadHandler client) {
        if (hasStarted) {
            return false;
        }

        return players.removeIf(player -> player.getClient() == client);
    }

    public void start() {
        hasStarted = true;

        for (int i = 0; i < rounds; i++) {
            cards.add(cardStackTake.takeCard());

            players.forEach(player -> {
                if (player.distribute(new ArrayList<>(cardStackTake.takeCards(2)))) {
                    cards.add(cardStackTake.takeCard());

                    if (Card.getSum(cards) != 21) {
                        player.triggerBlackjack();
                    }
                }
            });

            // end of round
            players.forEach(player -> cardStackPlace.putCardsBack(player.takeCardsAway()));

            cardStackPlace.putCardsBack(cards);
            cards.clear();
        }
    }
}
