package com.blackjacktrainer.game;

/**
 * Calculates points awarded for a correct answer.
 *
 * <p>Score formula:
 * <pre>
 *   base = 100
 *   level bonus  = level × 10
 *   streak bonus = streak × 5      (capped at 100)
 *   speed bonus  = max(0, 50 − responseTimeSeconds × 5)
 * </pre>
 */
public class ScoreManager {

    private static final int BASE_POINTS          = 100;
    private static final int LEVEL_MULTIPLIER     = 10;
    private static final int STREAK_MULTIPLIER    = 5;
    private static final int MAX_STREAK_BONUS     = 100;
    private static final int SPEED_BONUS_MAX      = 50;
    private static final double SPEED_DECAY       = 5.0;

    /**
     * Calculates points for a correct answer.
     *
     * @param level              current game level (1–10)
     * @param streak             consecutive correct answers so far
     * @param responseTimeSeconds time taken to submit the answer
     * @return points to award
     */
    public int calculatePoints(int level, int streak, double responseTimeSeconds) {
        int levelBonus  = level * LEVEL_MULTIPLIER;
        int streakBonus = Math.min(streak * STREAK_MULTIPLIER, MAX_STREAK_BONUS);
        int speedBonus  = (int) Math.max(0, SPEED_BONUS_MAX - responseTimeSeconds * SPEED_DECAY);
        return BASE_POINTS + levelBonus + streakBonus + speedBonus;
    }

    /**
     * Returns a short description of the current streak status.
     * Used for UI tooltips.
     */
    public String streakLabel(int streak) {
        if (streak >= 20) return "🔥 LEGENDARY";
        if (streak >= 10) return "⚡ ON FIRE";
        if (streak >= 5)  return "✨ HOT STREAK";
        if (streak >= 3)  return "👍 NICE";
        return "";
    }
}
