package com.blackjacktrainer.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ScoreManagerTest {

    private final ScoreManager sm = new ScoreManager();

    @Test
    void baseScoreIsAtLeast100() {
        int pts = sm.calculatePoints(1, 0, 0.0);
        assertTrue(pts >= 100);
    }

    @Test
    void higherLevelGivesMorePoints() {
        int low  = sm.calculatePoints(1, 0, 5.0);
        int high = sm.calculatePoints(8, 0, 5.0);
        assertTrue(high > low);
    }

    @Test
    void streakBonusIsCappedAt100() {
        int withSmallStreak = sm.calculatePoints(1, 5,  0.0);
        int withHugeStreak  = sm.calculatePoints(1, 50, 0.0);
        // The delta should not grow beyond cap
        assertTrue(withHugeStreak - withSmallStreak <= 100);
    }

    @Test
    void fastAnswerGivesSpeedBonus() {
        int fast = sm.calculatePoints(1, 0, 0.0);
        int slow = sm.calculatePoints(1, 0, 20.0);
        assertTrue(fast > slow);
    }

    @Test
    void streakLabel_legendaryAt20() {
        assertTrue(sm.streakLabel(20).contains("LEGENDARY"));
    }
}
