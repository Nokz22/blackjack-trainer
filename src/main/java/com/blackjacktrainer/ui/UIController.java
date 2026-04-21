package com.blackjacktrainer.ui;

import com.blackjacktrainer.core.Card;
import com.blackjacktrainer.core.HiLoCountSystem;
import com.blackjacktrainer.game.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Bridges UI events (button clicks, timer callbacks) with game logic.
 *
 * <p>All JavaFX UI updates run on the FX application thread.
 * This class never directly manipulates nodes — it communicates
 * back to {@link GameView} through injected callbacks.
 */
public class UIController {

    // ── Callbacks injected by GameView ────────────────────────────────────────
    private Consumer<Card>        onShowCard;
    private Consumer<RoundResult> onRoundComplete;
    private Runnable              onRoundStart;
    private Runnable              onGameOver;
    private Consumer<Integer>     onTimerTick;   // seconds remaining

    // ── State ─────────────────────────────────────────────────────────────────
    private GameController   controller;
    private StatsPersistence persistence;
    private Timeline         cardTimeline;
    private Timeline         answerTimer;
    private long             roundStartTime;
    private List<Card>       pendingCards;

    public UIController() {
        this.persistence = new StatsPersistence();
    }

    // ── Session lifecycle ─────────────────────────────────────────────────────

    /** Creates a new game session for the selected mode. */
    public void startSession(GameMode mode) {
        controller = new GameController(mode, new HiLoCountSystem());
        StatsPersistence.Stats stats = persistence.load();
        controller.loadPersistedStats(stats.highScore(), stats.bestStreak());
    }

    /** Deals cards for the next round with timed reveal animations. */
    public void startNextRound() {
        if (!controller.getState().isActive()) return;

        List<Card> cards = controller.startRound();
        pendingCards     = cards;
        roundStartTime   = System.currentTimeMillis();

        if (onRoundStart != null) onRoundStart.run();

        double delay = controller.getCurrentLevel().getCardDelaySeconds();
        animateCardReveal(cards, delay);
    }

    /**
     * Called when the player confirms their count guess.
     *
     * @param playerCount the integer the player typed
     */
    public void submitAnswer(int playerCount) {
        cancelTimers();
        double elapsed = (System.currentTimeMillis() - roundStartTime) / 1000.0;
        RoundResult result = controller.submitAnswer(playerCount, elapsed);

        if (!controller.getState().isActive()) {
            persistence.save(controller.getState());
            if (onGameOver != null) Platform.runLater(onGameOver);
        } else {
            if (onRoundComplete != null) Platform.runLater(() -> onRoundComplete.accept(result));
        }
    }

    /** Ends the current session and persists stats. */
    public void endSession() {
        cancelTimers();
        if (controller != null) persistence.save(controller.getState());
    }

    // ── Accessors used by GameView ────────────────────────────────────────────

    public GameController getController() { return controller; }

    // ── Callback setters ──────────────────────────────────────────────────────

    public void setOnShowCard(Consumer<Card> cb)              { this.onShowCard = cb; }
    public void setOnRoundComplete(Consumer<RoundResult> cb)  { this.onRoundComplete = cb; }
    public void setOnRoundStart(Runnable cb)                  { this.onRoundStart = cb; }
    public void setOnGameOver(Runnable cb)                    { this.onGameOver = cb; }
    public void setOnTimerTick(Consumer<Integer> cb)          { this.onTimerTick = cb; }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Schedules each card to appear {@code delaySeconds} apart using a
     * JavaFX {@link Timeline}. After the last card, starts the answer timer.
     */
    private void animateCardReveal(List<Card> cards, double delaySeconds) {
        cardTimeline = new Timeline();
        for (int i = 0; i < cards.size(); i++) {
            final Card card = cards.get(i);
            KeyFrame kf = new KeyFrame(Duration.seconds(i * delaySeconds), e -> {
                if (onShowCard != null) onShowCard.accept(card);
            });
            cardTimeline.getKeyFrames().add(kf);
        }
        cardTimeline.setOnFinished(e -> startAnswerTimer());
        cardTimeline.play();
    }

    /** Starts the countdown if the current level has a time limit. */
    private void startAnswerTimer() {
        int limit = controller.getCurrentLevel().getAnswerTimeLimitSeconds();
        if (limit <= 0 || controller.getState().getMode() == GameMode.PRACTICE) return;

        final int[] remaining = {limit};
        answerTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            if (onTimerTick != null) onTimerTick.accept(remaining[0]);
            if (remaining[0] <= 0) {
                answerTimer.stop();
                submitAnswer(Integer.MIN_VALUE); // force wrong answer on timeout
            }
        }));
        answerTimer.setCycleCount(limit);
        answerTimer.play();
    }

    private void cancelTimers() {
        if (cardTimeline != null) cardTimeline.stop();
        if (answerTimer  != null) answerTimer.stop();
    }
}
