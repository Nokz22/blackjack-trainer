package com.blackjacktrainer.core;

/**
 * Strategy interface for card-counting systems.
 *
 * <p>Implementations define the point value assigned to each card.
 * This design allows new systems (Hi-Opt I, KO, Omega II…) to be
 * added without touching any game logic.
 */
public interface CountSystem {

    /** Returns the count delta for a single card. */
    int valueFor(Card card);

    /** Human-readable name displayed in the UI. */
    String getName();

    /**
     * Calculates the running count for a list of cards.
     * Default implementation iterates over the hand; override for efficiency.
     */
    default int runningCount(Hand hand) {
        return hand.getCards().stream()
                .mapToInt(this::valueFor)
                .sum();
    }
}
