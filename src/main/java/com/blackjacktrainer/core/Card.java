package com.blackjacktrainer.core;

/**
 * Immutable representation of a playing card.
 * Suit and rank are decoupled from counting logic.
 */
public final class Card {

    public enum Suit {
        HEARTS("♥"), DIAMONDS("♦"), CLUBS("♣"), SPADES("♠");

        private final String symbol;

        Suit(String symbol) { this.symbol = symbol; }

        public String getSymbol() { return symbol; }
    }

    public enum Rank {
        TWO("2", 2),
        THREE("3", 3),
        FOUR("4", 4),
        FIVE("5", 5),
        SIX("6", 6),
        SEVEN("7", 7),
        EIGHT("8", 8),
        NINE("9", 9),
        TEN("10", 10),
        JACK("J", 10),
        QUEEN("Q", 10),
        KING("K", 10),
        ACE("A", 11);

        private final String display;
        private final int value;

        Rank(String display, int value) {
            this.display = display;
            this.value = value;
        }

        public String getDisplay() { return display; }
        public int getValue() { return value; }
    }

    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() { return rank; }
    public Suit getSuit() { return suit; }

    /** Returns true if this card belongs to the red suits. */
    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }

    @Override
    public String toString() {
        return rank.getDisplay() + suit.getSymbol();
    }
}
