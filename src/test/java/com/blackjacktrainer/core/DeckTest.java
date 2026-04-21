package com.blackjacktrainer.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Deck")
class DeckTest {

    @Test
    @DisplayName("Single deck has 52 cards")
    void singleDeckHas52Cards() {
        Deck deck = new Deck(1);
        assertEquals(52, deck.remainingCards());
    }

    @Test
    @DisplayName("Four-deck shoe has 208 cards")
    void fourDeckShoeHas208Cards() {
        Deck deck = new Deck(4);
        assertEquals(208, deck.remainingCards());
    }

    @Test
    @DisplayName("Dealing reduces remaining count")
    void dealingReducesCount() {
        Deck deck = new Deck(1);
        deck.deal();
        assertEquals(51, deck.remainingCards());
    }

    @Test
    @DisplayName("dealCards returns correct number of cards")
    void dealCardsReturnsCorrectCount() {
        Deck deck = new Deck(1);
        List<Card> hand = deck.dealCards(5);
        assertEquals(5, hand.size());
    }

    @Test
    @DisplayName("All 52 unique rank/suit combinations present")
    void allCombinationsPresent() {
        Deck deck = new Deck(1);
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 52; i++) seen.add(deck.deal().toString());
        assertEquals(52, seen.size());
    }

    @Test
    @DisplayName("Illegal deck count throws exception")
    void illegalDeckCountThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Deck(0));
    }
}
