package at.htlleonding.blackjack.server.contents;

import at.htlleonding.blackjack.server.game.Card;

public record PlayerCardContent(String name, Card[] cards) {
}
