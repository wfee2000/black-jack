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
            executeRound();
        }
    }

    private void executeRound() {
        cards.add(cardStackTake.takeCard());

        players.forEach(player -> {
            player.distribute(new ArrayList<>(cardStackTake.takeCards(2)));

            if (Card.getSum(player.getCards()) > 20) {
                player.isOut(true);
                player.hasBlackJack(true);
            }
        });

        while (!players.stream().allMatch(Player::isOut)) {
            players.stream().filter(player -> !player.isOut()).forEach(player -> {
                switch (player.getClient().requireCall()) {
                    case Hit -> player.hit(cardStackTake.takeCard());
                    case Stay -> player.stay();
                    case DoubleDown -> player.doubleDown(cardStackTake.takeCard());
                    case Split -> player.split();
                    case Surrender -> player.surrender();
                }

                if (!player.isOut()) {
                    player.isOut(Card.getSum(player.getCards()) > 21);
                }
            });
        }

        cards.add(cardStackTake.takeCard());

        if (Card.getSum(cards) == 21) {
            players.forEach(player -> {
                if (!player.hasBlackJack()) {
                    player.triggerLoose();
                } else {
                    player.triggerDraw();
                }
            });
        }

        // players finished playing
        while (Card.getSum(cards) < 17) {
            cards.add(cardStackTake.takeCard());
        }

        players.forEach(player -> {
            if (player.hasBlackJack()) {
                player.triggerBlackjack();
                return;
            }

            int playerSum = Card.getSum(player.getCards());
            int sum = Card.getSum(cards);

            if (sum < playerSum) {
                player.triggerWin();
            } else if (sum == playerSum) {
                player.triggerDraw();
            } else {
                player.triggerLoose();
            }
        });

        // end of round
        players.forEach(player -> cardStackPlace.putCardsBack(player.takeCardsAway()));

        cardStackPlace.putCardsBack(cards);
        cards.clear();
    }
}
