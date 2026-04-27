package com.blackjacktrainer.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages level progression and provides level configurations.
 *
 * <p>Levels increase in difficulty by:
 * <ol>
 *   <li>Showing more cards per round</li>
 *   <li>Reducing the delay between cards</li>
 *   <li>Reducing the answer time limit</li>
 * </ol>
 *
 * <p>The answer time limit is defined for every level. Whether that limit is
 * actually enforced is a decision made by the game mode, not by this class.
 * {@link GameController#getAnswerTimeLimit()} handles that responsibility.
 *
 * Rounds per level before advancing: {@link #ROUNDS_TO_ADVANCE}.
 */
public class LevelManager {

    /** Correct answers in a row needed to advance one level. */
    public static final int ROUNDS_TO_ADVANCE = 5;

    /** Maximum level cap. */
    public static final int MAX_LEVEL = 10;

    private final List<LevelConfig> levels;

    public LevelManager() {
        levels = buildLevelTable();
    }

    /**
     * Returns the config for the given level number (1-indexed).
     * Clamps to MAX_LEVEL if exceeded.
     */
    public LevelConfig getConfig(int level) {
        int idx = Math.min(level, MAX_LEVEL) - 1;
        return levels.get(idx);
    }

    /**
     * Decides if the player should advance based on their current streak.
     *
     * @param currentLevel current level (1-indexed)
     * @param streak       consecutive correct answers since last mistake
     * @return {@code true} if a level-up should occur
     */
    public boolean shouldAdvance(int currentLevel, int streak) {
        return currentLevel < MAX_LEVEL && streak > 0 && streak % ROUNDS_TO_ADVANCE == 0;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Hand-tuned progression table.
     *
     * <p>Every level now carries an answer time limit. Levels 1–3 receive the
     * same 20 s limit as level 4, matching the Timed-mode experience.
     * Whether the limit is enforced depends on the active {@link GameMode};
     * see {@link GameController#getAnswerTimeLimit()}.
     *
     * Pattern: more cards → faster pace → tighter answer window.
     */
    private List<LevelConfig> buildLevelTable() {
        List<LevelConfig> table = new ArrayList<>();
        //                  lvl  cards  delay(s)  answerLimit(s)
        table.add(new LevelConfig(1,   4,    1.8,  20));
        table.add(new LevelConfig(2,   5,    1.5,  20));
        table.add(new LevelConfig(3,   6,    1.3,  20));
        table.add(new LevelConfig(4,   6,    1.1,  20));
        table.add(new LevelConfig(5,   7,    1.0,  18));
        table.add(new LevelConfig(6,   7,    0.9,  15));
        table.add(new LevelConfig(7,   8,    0.8,  12));
        table.add(new LevelConfig(8,   8,    0.7,  10));
        table.add(new LevelConfig(9,   9,    0.6,   8));
        table.add(new LevelConfig(10, 10,    0.5,   6));
        return table;
    }
}
