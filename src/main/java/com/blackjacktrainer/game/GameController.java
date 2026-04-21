package com.blackjacktrainer.game;

import com.blackjacktrainer.core.*;

import java.util.List;

/**
 * Central orchestrator for a game session.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Deals rounds of cards from the deck</li>
 *   <li>Validates player answers against the active {@link CountSystem}</li>
 *   <li>Delegates scoring to {@link ScoreManager}</li>
 *   <li>Delegates level progression to {@link LevelManager}</li>
 *   <li>Updates {@link GameState}</li>
 * </ul>
 *
 * The UI layer calls {@link #startRound()} to get the next hand,
 * and {@link #submitAnswer(int, double)} when the player confirms.
 */
public class GameController {

    private final GameState    state;
    private final Deck         deck;
    private final CountSystem  countSystem;
    private final LevelManager levelManager;
    private final ScoreManager scoreManager;
    private final Hand         currentHand;

    public GameController(GameMode mode, CountSystem countSystem) {
        this.state        = new GameState(mode);
        this.deck         = new Deck(4); // 4-deck shoe is standard
        this.countSystem  = countSystem;
        this.levelManager = new LevelManager();
        this.scoreManager = new ScoreManager();
        this.currentHand  = new Hand();
    }

    /** Applies persisted stats (high score, best streak) at session start. */
    public void loadPersistedStats(int highScore, int bestStreak) {
        state.applyPersisted(highScore, bestStreak);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Prepares a new round: deals {@code cardCount} cards into the current hand.
     *
     * @return the list of cards to display, in deal order
     */
    public List<Card> startRound() {
        currentHand.clear();
        LevelConfig config = levelManager.getConfig(state.getLevel());
        List<Card> dealt   = deck.dealCards(config.getCardCount());
        dealt.forEach(currentHand::addCard);
        return List.copyOf(dealt);
    }

    /**
     * Evaluates the player's answer for the current round.
     *
     * @param playerCount         the running count the player entered
     * @param responseTimeSeconds elapsed time since the last card was shown
     * @return a {@link RoundResult} describing the outcome
     */
    public RoundResult submitAnswer(int playerCount, double responseTimeSeconds) {
        int correctCount = countSystem.runningCount(currentHand);
        boolean correct  = playerCount == correctCount;

        int points    = 0;
        boolean levelUp = false;

        if (correct) {
            points = scoreManager.calculatePoints(state.getLevel(), state.getStreak(), responseTimeSeconds);
            state.recordCorrect();
            state.incrementScore(points);
            if (levelManager.shouldAdvance(state.getLevel(), state.getStreak())) {
                state.incrementLevel();
                levelUp = true;
            }
        } else {
            state.recordIncorrect();
            // Endless mode ends on first mistake
            if (state.getMode() == GameMode.ENDLESS) {
                state.setActive(false);
            }
        }

        return new RoundResult(
                currentHand.getCards(),
                correctCount,
                playerCount,
                correct,
                points,
                state.getStreak(),
                levelUp
        );
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public GameState    getState()        { return state; }
    public LevelConfig  getCurrentLevel() { return levelManager.getConfig(state.getLevel()); }
    public CountSystem  getCountSystem()  { return countSystem; }
    public ScoreManager getScoreManager() { return scoreManager; }
}
