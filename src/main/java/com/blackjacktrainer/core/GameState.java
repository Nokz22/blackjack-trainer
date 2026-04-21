package com.blackjacktrainer.core;

import com.blackjacktrainer.game.GameMode;

/**
 * Snapshot of the game state at any point in time.
 * This is a pure data carrier — no logic lives here.
 */
public class GameState {

    private int score;
    private int level;
    private int streak;
    private int totalRounds;
    private int correctRounds;
    private int highScore;
    private int bestStreak;
    private GameMode mode;
    private boolean active;

    public GameState(GameMode mode) {
        this.mode    = mode;
        this.score   = 0;
        this.level   = 1;
        this.streak  = 0;
        this.totalRounds   = 0;
        this.correctRounds = 0;
        this.highScore     = 0;
        this.bestStreak    = 0;
        this.active  = true;
    }

    // ── Accessors ────────────────────────────────────────────────────────────

    public int     getScore()          { return score; }
    public int     getLevel()          { return level; }
    public int     getStreak()         { return streak; }
    public int     getTotalRounds()    { return totalRounds; }
    public int     getCorrectRounds()  { return correctRounds; }
    public int     getHighScore()      { return highScore; }
    public int     getBestStreak()     { return bestStreak; }
    public GameMode getMode()          { return mode; }
    public boolean isActive()          { return active; }

    public double getAccuracy() {
        return totalRounds == 0 ? 0.0 : (double) correctRounds / totalRounds * 100.0;
    }

    // ── Mutators (called only by game logic classes) ──────────────────────────

    public void incrementScore(int points) {
        score += points;
        if (score > highScore) highScore = score;
    }

    public void incrementLevel() { level++; }

    public void recordCorrect() {
        totalRounds++;
        correctRounds++;
        streak++;
        if (streak > bestStreak) bestStreak = streak;
    }

    public void recordIncorrect() {
        totalRounds++;
        streak = 0;
    }

    public void setActive(boolean active) { this.active = active; }

    public void applyPersisted(int highScore, int bestStreak) {
        this.highScore  = highScore;
        this.bestStreak = bestStreak;
    }
}
