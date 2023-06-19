package at.htlleonding.blackjack.server.game;

import at.htlleonding.blackjack.server.ClientThreadHandler;
import at.htlleonding.blackjack.server.contents.CardContent;
import at.htlleonding.blackjack.server.contents.MessageContent;
import at.htlleonding.blackjack.server.contents.PlayerCardContent;
import at.htlleonding.blackjack.server.contents.PlayerContent;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dealer {
    private final List<Player> players;
    private final int rounds;
    private final int maxPlayers;
    private final CardStack cardStackTake;
    private final CardStack cardStackPlace;
    private final List<Card> cards;
    private boolean hasStarted;
    private final int id;
    private final String password;
    private final String name;

    public Dealer(int id, int maxPlayers, int rounds, String name, String password) {
        this.id = id;
        this.rounds = rounds;
        this.name = name;
        this.password = password;
        this.maxPlayers = maxPlayers;
        cards = new ArrayList<>();
        players = new ArrayList<>();
        cardStackTake = new CardStack(true);
        cardStackPlace = new CardStack(false);
    }

    public void addPlayer(ClientThreadHandler client) {
        if (hasStarted || maxPlayers == players.size()) {
            return;
        }

        Player newPlayer = new Player(client);
        try {
            newPlayer.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(
                    new MessageContent("update", ClientThreadHandler.mapper.writeValueAsString(
                            players.stream().map(playerInArray -> new PlayerContent(playerInArray.getClient()
                                    .getName())).toArray()))));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        players.forEach(player -> {
            try {
                player.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(
                        new MessageContent("add", ClientThreadHandler.mapper.writeValueAsString(
                                new PlayerContent(newPlayer.getClient().getName())))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        players.add(newPlayer);
    }

    public boolean removePlayer(ClientThreadHandler client) {
        if (hasStarted) {
            return false;
        }

        boolean wasRemoved = players.removeIf(player -> player.getClient() == client);
        players.forEach(player -> {
            try {
                player.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(
                        new MessageContent("update", ClientThreadHandler.mapper.writeValueAsString(
                                players.stream().map(playerInArray -> new PlayerContent(playerInArray.getClient()
                                        .getName())).toArray()))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        return wasRemoved;
    }

    public boolean start() {
        hasStarted = true;

        players.forEach(player -> {
            try {
                player.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(
                        new MessageContent("start", "")));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        Runnable runnable = () -> {
            for (int i = 0; i < rounds; i++) {
                new Thread(this::executeRound);
            }
        };

        new Thread(runnable).start();
        return true;
    }

    private void executeRound() {
        players.forEach(player -> player.setBet(player.getClient().requireBet()));
        addDealerCard();

        players.forEach(player -> {
            if (player.distribute(new ArrayList<>(cardStackTake.takeCards(2)))) {
                player.isOut(true);
                player.hasBlackJack(true);
            } else {
                player.isOut(false);
            }
        });

        players.forEach(player -> {
            try {
                player.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(new MessageContent("distributed",
                        ClientThreadHandler.mapper.writeValueAsString(players.stream()
                                .map(playerCards -> new PlayerCardContent(playerCards.getClient().getName(),
                                        playerCards.getCards().toArray(new Card[0]))).toArray()))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });

        while (!players.stream().allMatch(Player::isOut)) {
            players.stream().filter(player -> !player.isOut()).forEach(player -> {
                switch (player.getClient().requireCall()) {
                    case Hit -> player.hit(cardStackTake.takeCard());
                    case Stay -> player.stay();
                    case DoubleDown -> player.doubleDown(cardStackTake.takeCard());
                    case Surrender -> player.surrender();
                }

                if (!player.isOut()) {
                    player.isOut(Card.getSum(player.getCards()) > 21);
                }
            });
        }

        addDealerCard();

        if (Card.getSum(cards) == 21) {
            players.forEach(player -> {
                if (!player.hasBlackJack()) {
                    player.triggerLoose();
                } else {
                    player.triggerDraw();
                }
            });
        } else {
            players.forEach(player -> {
                try {
                    player.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(
                            new MessageContent("no-blackjack?", "")));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }

        // players finished playing
        while (Card.getSum(cards) < 17) {
            addDealerCard();
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

    public void addDealerCard() {
        Card card = cardStackTake.takeCard();
        cards.add(card);

        players.forEach(player -> {
            try {
                player.getClient().sendMessage(ClientThreadHandler.mapper.writeValueAsString(
                        new MessageContent("dealerAdd", ClientThreadHandler.mapper.writeValueAsString(
                                new CardContent(card)))));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
    }

    public int getId() {
        return id;
    }
    public int getPlayerCount() {
        return players.size();
    }
    public int getMaxPlayers() {
        return maxPlayers;
    }
    public boolean hasStarted() {
        return hasStarted;
    }
    public String getPassword() {
        return password;
    }
    public String getName() {
        return name;
    }
    public List<Player> getPlayers(){
        return players;
    }
}
