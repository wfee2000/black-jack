package at.htlleonding.blackjack.server.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class CardStack {
    private final Stack<Card> stack;

    public CardStack(boolean isFull) {
        stack = new Stack<>();
        if (isFull) {
            initStack();
        }
    }

    private void initStack() {
        List<Card> cards = new ArrayList<>();

        for (int i = 0; i < 6; i++) {
            Arrays.stream(Sign.values()).forEach(
                    sign -> Arrays.stream(Value.values()).forEach(value -> cards.add(new Card(sign, value))));
        }

        while (!cards.isEmpty()) {
            stack.push(cards.remove((int)(Math.random() * cards.size())));
        }
    }

    public Card takeCard() {
        return stack.pop();
    }

    public List<Card> takeCards(int amount) {
        List<Card> cards = stack.stream().limit(amount).toList();
        stack.removeAll(cards);
        return cards;
    }

    public void putCardsBack(List<Card> cards) {
        stack.addAll(cards);
        shuffle();
    }

    public void shuffle() {
        List<Card> cards = new ArrayList<>(stack.stream().toList());
        stack.clear();

        while (!cards.isEmpty()) {
            stack.push(cards.remove((int)(Math.random() * cards.size())));
        }
    }
}
