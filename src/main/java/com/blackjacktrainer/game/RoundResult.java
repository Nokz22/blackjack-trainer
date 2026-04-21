package com.blackjacktrainer.game;

import com.blackjacktrainer.core.Card;
import java.util.List;

/**
 * Immutable result produced after a player submits their count for a round.
 */
public final class RoundResult {

    private final List<Card> cards;
    private final int correctCount;
    private final int playerCount;
    private final boolean correct;
    private final int pointsAwarded;
    private final int newStreak;
    private final boolean leveledUp;

    public RoundResult(
            List<Card> cards,
            int correctCount,
            int playerCount,
            boolean correct,
            int pointsAwarded,
            int newStreak,
            boolean leveledUp) {
        this.cards         = List.copyOf(cards);
        this.correctCount  = correctCount;
        this.playerCount   = playerCount;
        this.correct       = correct;
        this.pointsAwarded = pointsAwarded;
        this.newStreak     = newStreak;
        this.leveledUp     = leveledUp;
    }

    public List<Card> getCards()        { return cards; }
    public int  getCorrectCount()       { return correctCount; }
    public int  getPlayerCount()        { return playerCount; }
    public boolean isCorrect()          { return correct; }
    public int  getPointsAwarded()      { return pointsAwarded; }
    public int  getNewStreak()          { return newStreak; }
    public boolean isLeveledUp()        { return leveledUp; }
}
