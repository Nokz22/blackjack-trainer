package com.blackjacktrainer.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Represents one or more shuffled decks of playing cards.
 * Supports multi-deck configurations common in casino blackjack.
 */
public class Deck {

    private final Deque<Card> cards;
    private final int numberOfDecks;

    public Deck(int numberOfDecks) {
        if (numberOfDecks < 1) throw new IllegalArgumentException("Must have at least one deck");
        this.numberOfDecks = numberOfDecks;
        this.cards = new ArrayDeque<>();
        build();
    }

    /** Builds and shuffles the deck(s). */
    private void build() {
        List<Card> fresh = new ArrayList<>();
        for (int d = 0; d < numberOfDecks; d++) {
            for (Card.Suit suit : Card.Suit.values()) {
                for (Card.Rank rank : Card.Rank.values()) {
                    fresh.add(new Card(rank, suit));
                }
            }
        }
        Collections.shuffle(fresh);
        cards.clear();
        cards.addAll(fresh);
    }

    /** Deals a single card from the top. */
    public Card deal() {
        if (cards.isEmpty()) build(); // auto-reshuffle when exhausted
        return cards.pop();
    }

    /** Deals a list of {@code count} cards sequentially. */
    public List<Card> dealCards(int count) {
        List<Card> hand = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            hand.add(deal());
        }
        return hand;
    }

    public int remainingCards() { return cards.size(); }
    public int getNumberOfDecks() { return numberOfDecks; }
}
