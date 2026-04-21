package com.blackjacktrainer.game;

import com.blackjacktrainer.core.HiLoCountSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GameController")
class GameControllerTest {

    private GameController controller;

    @BeforeEach
    void setUp() {
        controller = new GameController(GameMode.PRACTICE, new HiLoCountSystem());
    }

    @Test @DisplayName("startRound returns the expected number of cards for level 1")
    void startRoundDealsCorrectCount() {
        var cards = controller.startRound();
        int expected = new LevelManager().getConfig(1).getCardCount();
        assertEquals(expected, cards.size());
    }

    @Test @DisplayName("Correct answer increases score")
    void correctAnswerIncreasesScore() {
        var cards = controller.startRound();
        int correct = new HiLoCountSystem().runningCount(handOf(cards));
        int before  = controller.getState().getScore();
        RoundResult result = controller.submitAnswer(correct, 1.0);
        assertTrue(result.isCorrect());
        assertTrue(controller.getState().getScore() > before);
    }

    @Test @DisplayName("Incorrect answer keeps score unchanged and resets streak")
    void incorrectAnswerResetsStreak() {
        controller.startRound();
        int before = controller.getState().getScore();
        RoundResult result = controller.submitAnswer(Integer.MIN_VALUE, 1.0);
        assertFalse(result.isCorrect());
        assertEquals(before, controller.getState().getScore());
        assertEquals(0, controller.getState().getStreak());
    }

    @Test @DisplayName("Endless mode deactivates on first wrong answer")
    void endlessModeDeactivatesOnWrong() {
        GameController endless = new GameController(GameMode.ENDLESS, new HiLoCountSystem());
        endless.startRound();
        endless.submitAnswer(Integer.MIN_VALUE, 1.0);
        assertFalse(endless.getState().isActive());
    }

    @Test @DisplayName("Practice mode stays active after wrong answer")
    void practiceModeStaysActive() {
        controller.startRound();
        controller.submitAnswer(Integer.MIN_VALUE, 1.0);
        assertTrue(controller.getState().isActive());
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private com.blackjacktrainer.core.Hand handOf(List<com.blackjacktrainer.core.Card> cards) {
        var hand = new com.blackjacktrainer.core.Hand();
        cards.forEach(hand::addCard);
        return hand;
    }
}
