package com.blackjacktrainer.game;

/**
 * Immutable configuration for a single difficulty level.
 */
public final class LevelConfig {

    private final int level;
    private final int cardCount;
    private final double cardDelaySeconds;
    private final int answerTimeLimitSeconds; // 0 = no limit

    public LevelConfig(int level, int cardCount, double cardDelaySeconds, int answerTimeLimitSeconds) {
        this.level                  = level;
        this.cardCount              = cardCount;
        this.cardDelaySeconds       = cardDelaySeconds;
        this.answerTimeLimitSeconds = answerTimeLimitSeconds;
    }

    public int    getLevel()                  { return level; }
    public int    getCardCount()              { return cardCount; }
    public double getCardDelaySeconds()       { return cardDelaySeconds; }
    public int    getAnswerTimeLimitSeconds()  { return answerTimeLimitSeconds; }
    public boolean hasTimeLimit()             { return answerTimeLimitSeconds > 0; }

    @Override
    public String toString() {
        return String.format("Level %d | %d cards @ %.1fs | limit: %ds",
                level, cardCount, cardDelaySeconds, answerTimeLimitSeconds);
    }
}
