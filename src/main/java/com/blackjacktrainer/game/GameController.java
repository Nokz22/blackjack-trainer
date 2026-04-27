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
 * <p>Timer behaviour per mode:
 * <ul>
 *   <li>{@link GameMode#PRACTICE} — timer is active; a wrong answer or
 *       timeout does <em>not</em> end the session, allowing the player to
 *       keep practising at their own rhythm.</li>
 *   <li>{@link GameMode#TIMED}    — timer is active; timeout counts as a
 *       wrong answer but the session continues.</li>
 *   <li>{@link GameMode#ENDLESS}  — timer is active; the first wrong answer
 *       or timeout ends the session.</li>
 * </ul>
 *
 * The UI layer calls {@link #startRound()} to get the next hand,
 * {@link #getAnswerTimeLimit()} to know how long to show the countdown,
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
     * Returns the answer time limit in seconds for the current round,
     * taking the active game mode into account.
     *
     * <p>The time limit defined in {@link LevelConfig} applies to all timed
     * modes. {@link GameMode#PRACTICE} shares the same per-level limits so
     * players train under realistic pressure, but a timeout in Practice mode
     * does not penalise the session the same way it does in other modes.
     *
     * <p>The UI is responsible for starting and stopping the countdown;
     * this method only reports the duration to use.
     *
     * @return seconds allowed to answer, always {@code > 0} for every mode
     */
    public int getAnswerTimeLimit() {
        return levelManager.getConfig(state.getLevel()).getAnswerTimeLimitSeconds();
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
            // Only Endless mode ends on a wrong answer — Practice and Timed continue.
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
