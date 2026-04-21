package com.blackjacktrainer.game;

/**
 * Available game modes, each with a distinct pacing and failure condition.
 */
public enum GameMode {

    /** No timer — focus on building accuracy at your own pace. */
    PRACTICE("Practice Mode", "No time limit. Build your accuracy."),

    /** A countdown timer pressures each answer. */
    TIMED("Timed Mode", "Answer before the clock runs out!"),

    /** Rounds loop endlessly until the player makes a mistake. */
    ENDLESS("Endless Mode", "Keep going until you get one wrong.");

    private final String displayName;
    private final String description;

    GameMode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName()  { return displayName; }
    public String getDescription()  { return description; }
}
