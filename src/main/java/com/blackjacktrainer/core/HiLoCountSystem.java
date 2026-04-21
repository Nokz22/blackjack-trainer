package com.blackjacktrainer.core;

/**
 * Hi-Lo card counting system.
 *
 * <ul>
 *   <li>2–6  → +1 (low cards favour the dealer — remove them, count goes up)</li>
 *   <li>7–9  →  0 (neutral)</li>
 *   <li>10–A → -1 (high cards favour the player — remove them, count goes down)</li>
 * </ul>
 */
public class HiLoCountSystem implements CountSystem {

    @Override
    public int valueFor(Card card) {
        return switch (card.getRank()) {
            case TWO, THREE, FOUR, FIVE, SIX -> +1;
            case SEVEN, EIGHT, NINE          ->  0;
            case TEN, JACK, QUEEN, KING, ACE -> -1;
        };
    }

    @Override
    public String getName() {
        return "Hi-Lo";
    }
}
