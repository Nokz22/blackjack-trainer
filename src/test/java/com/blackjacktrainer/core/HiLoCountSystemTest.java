package com.blackjacktrainer.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HiLoCountSystemTest {

    private final CountSystem hiLo = new HiLoCountSystem();

    @Test
    void lowCards_returnPlusOne() {
        for (Card.Rank rank : new Card.Rank[]{
                Card.Rank.TWO, Card.Rank.THREE, Card.Rank.FOUR,
                Card.Rank.FIVE, Card.Rank.SIX}) {
            assertEquals(+1, hiLo.valueFor(new Card(rank, Card.Suit.HEARTS)),
                    "Expected +1 for " + rank);
        }
    }

    @Test
    void neutralCards_returnZero() {
        for (Card.Rank rank : new Card.Rank[]{
                Card.Rank.SEVEN, Card.Rank.EIGHT, Card.Rank.NINE}) {
            assertEquals(0, hiLo.valueFor(new Card(rank, Card.Suit.CLUBS)),
                    "Expected 0 for " + rank);
        }
    }

    @Test
    void highCards_returnMinusOne() {
        for (Card.Rank rank : new Card.Rank[]{
                Card.Rank.TEN, Card.Rank.JACK, Card.Rank.QUEEN,
                Card.Rank.KING, Card.Rank.ACE}) {
            assertEquals(-1, hiLo.valueFor(new Card(rank, Card.Suit.SPADES)),
                    "Expected -1 for " + rank);
        }
    }

    @Test
    void runningCount_mixedHand() {
        Hand hand = new Hand();
        hand.addCard(new Card(Card.Rank.TWO,   Card.Suit.HEARTS));
        hand.addCard(new Card(Card.Rank.KING,  Card.Suit.SPADES));
        hand.addCard(new Card(Card.Rank.FIVE,  Card.Suit.DIAMONDS));
        hand.addCard(new Card(Card.Rank.EIGHT, Card.Suit.CLUBS));
        assertEquals(1, hiLo.runningCount(hand));
    }

    @Test
    void runningCount_allHighCards_isNegative() {
        Hand hand = new Hand();
        for (int i = 0; i < 4; i++) {
            hand.addCard(new Card(Card.Rank.ACE, Card.Suit.HEARTS));
        }
        assertEquals(-4, hiLo.runningCount(hand));
    }

    @Test
    void systemName_isHiLo() {
        assertEquals("Hi-Lo", hiLo.getName());
    }
}
