package com.blackjacktrainer.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LevelManagerTest {

    private final LevelManager lm = new LevelManager();

    @Test
    void level1HasSlowDelay() {
        assertTrue(lm.getConfig(1).getCardDelaySeconds() >= 1.5);
    }

    @Test
    void higherLevelIsFaster() {
        double delay1 = lm.getConfig(1).getCardDelaySeconds();
        double delay5 = lm.getConfig(5).getCardDelaySeconds();
        assertTrue(delay5 < delay1);
    }

    @Test
    void shouldAdvance_afterNRoundsCorrect() {
        assertTrue(lm.shouldAdvance(1, LevelManager.ROUNDS_TO_ADVANCE));
    }

    @Test
    void shouldNotAdvance_atMaxLevel() {
        assertFalse(lm.shouldAdvance(LevelManager.MAX_LEVEL, LevelManager.ROUNDS_TO_ADVANCE));
    }

    @Test
    void configClampsToMaxLevel() {
        LevelConfig cfg = lm.getConfig(999);
        assertEquals(LevelManager.MAX_LEVEL, cfg.getLevel());
    }
}
