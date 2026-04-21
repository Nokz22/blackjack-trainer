package com.blackjacktrainer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a hand of cards dealt in one training round.
 * Immutable view is exposed via {@link #getCards()}.
 */
public class Hand {

    private final List<Card> cards = new ArrayList<>();

    public void addCard(Card card) {
        cards.add(card);
    }

    /** Returns an unmodifiable snapshot of the current hand. */
    public List<Card> getCards() {
        return Collections.unmodifiableList(cards);
    }

    public int size() { return cards.size(); }

    public void clear() { cards.clear(); }
}
