package com.blackjacktrainer.ui;

import com.blackjacktrainer.core.Card;
import com.blackjacktrainer.core.HiLoCountSystem;
import com.blackjacktrainer.game.GameMode;
import com.blackjacktrainer.game.RoundResult;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Root view for the Blackjack Counting Trainer.
 *
 * <p>Layout regions:
 * <pre>
 * ┌─────────────────────────────────────────────┐
 * │  Header: title | score | level | streak     │
 * ├─────────────────────────────────────────────┤
 * │  Card area (animated card reveal)           │
 * ├─────────────────────────────────────────────┤
 * │  Feedback banner                            │
 * ├─────────────────────────────────────────────┤
 * │  Input area: field + confirm button         │
 * ├─────────────────────────────────────────────┤
 * │  Stats bar: accuracy | best streak | games  │
 * └─────────────────────────────────────────────┘
 * </pre>
 */
public class GameView {

    private final Stage        stage;
    private final UIController uiController;
    private final HiLoCountSystem hintSystem = new HiLoCountSystem();

    // Tracks whether the current session is Practice Mode (for hint badges)
    private boolean practiceMode = false;

    // Root
    private BorderPane root;

    // Header
    private Label scoreLabel;
    private Label levelLabel;
    private Label streakLabel;
    private Label timerLabel;

    // Card area
    private HBox  cardArea;

    // Feedback
    private Label feedbackLabel;

    // Input
    private TextField countInput;
    private Button    confirmButton;
    private Button    nextButton;

    // Stats bar
    private Label accuracyLabel;
    private Label bestStreakLabel;
    private Label highScoreLabel;

    // ── Constructor ───────────────────────────────────────────────────────────

    public GameView(Stage stage) {
        this.stage        = stage;
        this.uiController = new UIController();
        buildUI();
        showModeSelection();
    }

    public Pane getRootPane() { return root; }

    // ── UI Construction ───────────────────────────────────────────────────────

    private void buildUI() {
        root = new BorderPane();
        root.getStyleClass().add("root-pane");

        root.setTop(buildHeader());
        root.setCenter(buildCenter());
        root.setBottom(buildBottomBar());

        // ── Global keyboard shortcuts ────────────────────────
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.Q) {
                handleQuit();
            }
        });
        // Ensure the pane can receive key events
        root.setFocusTraversable(true);
    }

    private HBox buildHeader() {
        HBox header = new HBox(24);
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("♠ Blackjack Trainer");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        levelLabel  = new Label("Level 1");
        scoreLabel  = new Label("Score: 0");
        streakLabel = new Label("");
        timerLabel  = new Label("");

        levelLabel.getStyleClass().add("header-stat");
        scoreLabel.getStyleClass().add("header-stat");
        streakLabel.getStyleClass().addAll("header-stat", "streak-label");
        timerLabel.getStyleClass().addAll("header-stat", "timer-label");

        header.getChildren().addAll(title, spacer, timerLabel, streakLabel, levelLabel, scoreLabel);
        return header;
    }

    private VBox buildCenter() {
        VBox center = new VBox(24);
        center.getStyleClass().add("center-pane");
        center.setAlignment(Pos.CENTER);

        // Card area
        cardArea = new HBox(12);
        cardArea.setAlignment(Pos.CENTER);
        cardArea.setMinHeight(130);

        // Feedback
        feedbackLabel = new Label("");
        feedbackLabel.getStyleClass().add("feedback-label");

        // Input row
        HBox inputRow = buildInputRow();

        center.getChildren().addAll(cardArea, feedbackLabel, inputRow);
        return center;
    }

    private HBox buildInputRow() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);

        Label hint = new Label("Running count:");
        hint.getStyleClass().add("input-hint");

        countInput = new TextField();
        countInput.getStyleClass().add("count-input");
        countInput.setPromptText("e.g.  -2");
        countInput.setPrefWidth(100);
        countInput.setDisable(true);

        // Submit on Enter
        countInput.setOnAction(e -> handleConfirm());

        confirmButton = new Button("Confirm  ↵");
        confirmButton.getStyleClass().add("btn-primary");
        confirmButton.setDisable(true);
        confirmButton.setOnAction(e -> handleConfirm());

        nextButton = new Button("Next Round  →");
        nextButton.getStyleClass().add("btn-secondary");
        nextButton.setVisible(false);
        nextButton.setOnAction(e -> handleNextRound());

        row.getChildren().addAll(hint, countInput, confirmButton, nextButton);
        return row;
    }

    private HBox buildBottomBar() {
        HBox bar = new HBox(32);
        bar.getStyleClass().add("stats-bar");
        bar.setAlignment(Pos.CENTER);

        accuracyLabel   = new Label("Accuracy: —");
        bestStreakLabel = new Label("Best Streak: 0");
        highScoreLabel  = new Label("High Score: 0");

        accuracyLabel.getStyleClass().add("stat-chip");
        bestStreakLabel.getStyleClass().add("stat-chip");
        highScoreLabel.getStyleClass().add("stat-chip");

        bar.getChildren().addAll(accuracyLabel, bestStreakLabel, highScoreLabel);
        return bar;
    }

    // ── Mode Selection Overlay ────────────────────────────────────────────────

    private void showModeSelection() {
        // Outer scroll so it fits smaller windows
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("menu-scroll");

        VBox overlay = new VBox(28);
        overlay.getStyleClass().add("overlay");
        overlay.setAlignment(Pos.CENTER);
        overlay.setFillWidth(true);

        // ── Title ────────────────────────────────────────────
        Text title = new Text("♠ Blackjack Counting Trainer");
        title.getStyleClass().add("overlay-title");

        // ── How to play ──────────────────────────────────────
        VBox howTo = buildInstructionsPanel();

        // ── Mode cards ───────────────────────────────────────
        Text modeTitle = new Text("Choose Your Mode");
        modeTitle.getStyleClass().add("section-title");

        VBox modeList = new VBox(12);
        modeList.setAlignment(Pos.CENTER);
        for (GameMode mode : GameMode.values()) {
            modeList.getChildren().add(buildModeCard(mode));
        }

        // ── Keyboard hint ────────────────────────────────────
        Label kbHint = new Label("Press Q or ESC at any time to quit to menu");
        kbHint.getStyleClass().add("kb-hint");

        overlay.getChildren().addAll(title, howTo, modeTitle, modeList, kbHint);
        scroll.setContent(overlay);
        root.setCenter(scroll);

        // Give focus to root so key events work immediately
        root.requestFocus();
    }

    private VBox buildInstructionsPanel() {
        VBox panel = new VBox(10);
        panel.getStyleClass().add("instructions-panel");
        panel.setMaxWidth(520);

        Label heading = new Label("📖  How to Play");
        heading.getStyleClass().add("instructions-heading");

        Label body = new Label(
                "Cards appear on screen one by one, simulating a real blackjack shoe.\n\n" +
                "Use the Hi-Lo system to track the running count as each card is revealed:\n" +
                "  • 2 – 6  →  +1   (low cards benefit the dealer)\n" +
                "  • 7 – 9  →   0   (neutral)\n" +
                "  • 10 / J / Q / K / A  →  −1   (high cards benefit the player)\n\n" +
                "After the last card, type your running count and press Enter or click Confirm.\n\n" +
                "💡  In Practice Mode a small badge above each card shows its Hi-Lo value,\n" +
                "       so you can learn the system at your own pace."
        );
        body.getStyleClass().add("instructions-body");
        body.setWrapText(true);

        // Score breakdown
        Label scoreHead = new Label("🏆  Scoring");
        scoreHead.getStyleClass().add("instructions-heading");

        Label scoreBody = new Label(
                "Points = 100 base  +  (level × 10)  +  (streak × 5, max 100)  +  speed bonus\n" +
                "Advance a level after 5 correct answers in a row."
        );
        scoreBody.getStyleClass().add("instructions-body");
        scoreBody.setWrapText(true);

        panel.getChildren().addAll(heading, body, scoreHead, scoreBody);
        return panel;
    }

    private VBox buildModeCard(GameMode mode) {
        VBox card = new VBox(6);
        card.getStyleClass().add("mode-card");
        card.setAlignment(Pos.CENTER_LEFT);
        card.setMaxWidth(360);

        Label name = new Label(mode.getDisplayName());
        name.getStyleClass().add("mode-name");
        Label desc = new Label(mode.getDescription());
        desc.getStyleClass().add("mode-desc");

        card.getChildren().addAll(name, desc);
        card.setOnMouseClicked(e -> startGame(mode));
        return card;
    }

    // ── Game Flow ─────────────────────────────────────────────────────────────

    private void startGame(GameMode mode) {
        practiceMode = (mode == GameMode.PRACTICE);
        uiController.startSession(mode);
        wireCallbacks();
        root.setCenter(buildCenter());
        uiController.startNextRound();
        root.requestFocus();
    }

    private void wireCallbacks() {
        uiController.setOnRoundStart(() -> {
            cardArea.getChildren().clear();
            feedbackLabel.setText("");
            feedbackLabel.getStyleClass().removeAll("feedback-correct", "feedback-wrong");
            countInput.clear();
            countInput.setDisable(false);
            confirmButton.setDisable(false);
            nextButton.setVisible(false);
        });

        uiController.setOnShowCard(this::revealCard);

        uiController.setOnRoundComplete(this::showRoundResult);

        uiController.setOnGameOver(this::showGameOver);

        uiController.setOnTimerTick(seconds -> timerLabel.setText(seconds > 0 ? "⏱ " + seconds + "s" : ""));
    }

    private void revealCard(Card card) {
        int hint = practiceMode ? hintSystem.valueFor(card) : CardNode.NO_HINT;
        CardNode node = new CardNode(card, hint);
        cardArea.getChildren().add(node);
        node.reveal();
    }

    private void showRoundResult(RoundResult result) {
        countInput.setDisable(true);
        confirmButton.setDisable(true);
        timerLabel.setText("");

        if (result.isCorrect()) {
            feedbackLabel.setText("✓ Correct!  +" + result.getPointsAwarded() + " pts"
                    + (result.isLeveledUp() ? "   🎉 Level Up!" : ""));
            feedbackLabel.getStyleClass().add("feedback-correct");
        } else {
            feedbackLabel.setText("✗ Wrong — correct count was " + result.getCorrectCount());
            feedbackLabel.getStyleClass().add("feedback-wrong");
        }

        // Animate feedback
        FadeTransition fade = new FadeTransition(Duration.millis(300), feedbackLabel);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        updateHeaderStats();
        nextButton.setVisible(true);
    }

    private void handleConfirm() {
        if (confirmButton.isDisable()) return;
        String raw = countInput.getText().trim();
        try {
            int value = Integer.parseInt(raw);
            uiController.submitAnswer(value);
        } catch (NumberFormatException e) {
            shakeNode(countInput);
        }
    }

    private void handleNextRound() {
        nextButton.setVisible(false);
        uiController.startNextRound();
    }

    private void showGameOver() {
        var state = uiController.getController().getState();

        VBox overlay = new VBox(20);
        overlay.getStyleClass().add("overlay");
        overlay.setAlignment(Pos.CENTER);

        Text title   = new Text("Game Over");
        title.getStyleClass().add("overlay-title");

        Label score   = new Label("Score: "         + state.getScore());
        Label streak  = new Label("Best Streak: "   + state.getBestStreak());
        Label acc     = new Label(String.format("Accuracy: %.1f%%", state.getAccuracy()));

        score.getStyleClass().add("gameover-stat");
        streak.getStyleClass().add("gameover-stat");
        acc.getStyleClass().add("gameover-stat");

        HBox buttons = new HBox(16);
        buttons.setAlignment(Pos.CENTER);

        Button playAgain = new Button("Play Again");
        playAgain.getStyleClass().add("btn-primary");
        playAgain.setOnAction(e -> startGame(uiController.getController().getState().getMode()));

        Button menuBtn = new Button("← Main Menu");
        menuBtn.getStyleClass().add("btn-secondary");
        menuBtn.setOnAction(e -> showModeSelection());

        buttons.getChildren().addAll(menuBtn, playAgain);
        overlay.getChildren().addAll(title, score, streak, acc, buttons);
        root.setCenter(overlay);
        root.requestFocus();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void handleQuit() {
        uiController.endSession();
        practiceMode = false;
        showModeSelection();
    }

    private void updateHeaderStats() {
        var state = uiController.getController().getState();
        scoreLabel.setText("Score: "  + state.getScore());
        levelLabel.setText("Level "   + state.getLevel());
        String sl = uiController.getController().getScoreManager().streakLabel(state.getStreak());
        streakLabel.setText(sl.isEmpty() ? "" : sl + " ×" + state.getStreak());
        accuracyLabel.setText(String.format("Accuracy: %.1f%%", state.getAccuracy()));
        bestStreakLabel.setText("Best Streak: " + state.getBestStreak());
        highScoreLabel.setText("High Score: "   + state.getHighScore());
    }

    /** Quick horizontal shake — used for invalid input. */
    private void shakeNode(javafx.scene.Node node) {
        Timeline shake = new Timeline(
                new KeyFrame(Duration.millis(0),   new KeyValue(node.translateXProperty(),  0)),
                new KeyFrame(Duration.millis(60),  new KeyValue(node.translateXProperty(), -8)),
                new KeyFrame(Duration.millis(120), new KeyValue(node.translateXProperty(),  8)),
                new KeyFrame(Duration.millis(180), new KeyValue(node.translateXProperty(), -6)),
                new KeyFrame(Duration.millis(240), new KeyValue(node.translateXProperty(),  6)),
                new KeyFrame(Duration.millis(300), new KeyValue(node.translateXProperty(),  0))
        );
        shake.play();
    }
}
