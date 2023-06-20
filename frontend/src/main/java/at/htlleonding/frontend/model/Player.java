package at.htlleonding.frontend.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Player {

    private List<Card> cards;
    private final String name;
    private int bet;

    public Player(String name){
        this.name = name;
    }

    public void addCards(Card[] newCards){
        this.cards.addAll(Arrays.stream(newCards).toList());
    }

    public void addCard(Card newCard){
        this.cards.add(newCard);
    }

    public List<Card> getCards(){
        return this.cards;
    }

    public String getName() {
        return name;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }
}
