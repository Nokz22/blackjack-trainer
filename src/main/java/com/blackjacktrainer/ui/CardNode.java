package com.blackjacktrainer.ui;

import com.blackjacktrainer.core.Card;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A single playing-card visual node.
 *
 * <p>Renders rank + suit centred on a styled rectangle.
 * Optionally shows a Hi-Lo hint badge at the top (Practice Mode).
 * Supports a {@link #reveal()} animation (fade-in + scale).
 */
public class CardNode extends StackPane {

    private static final double CARD_WIDTH  = 80;
    private static final double CARD_HEIGHT = 110;

    /** Sentinel: no hint badge shown. */
    public static final int NO_HINT = Integer.MIN_VALUE;

    private final Text  label;
    private final Card  card;

    /**
     * @param card      the card to display
     * @param countHint Hi-Lo value to show as a badge, or {@link #NO_HINT} to hide it
     */
    public CardNode(Card card, int countHint) {
        this.card = card;

        getStyleClass().add("playing-card");
        if (card.isRed()) getStyleClass().add("card-red");

        setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        setMaxSize(CARD_WIDTH, CARD_HEIGHT);

        // ── Card face label ──────────────────────────────────
        label = new Text(card.getRank().getDisplay() + "\n" + card.getSuit().getSymbol());
        label.getStyleClass().add("card-label");
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        StackPane.setAlignment(label, Pos.CENTER);

        getChildren().add(label);

        // ── Hi-Lo hint badge (Practice Mode only) ────────────
        if (countHint != NO_HINT) {
            String sign  = countHint > 0 ? "+" : "";
            Label  badge = new Label(sign + countHint);
            badge.getStyleClass().add("hilo-badge");
            if      (countHint > 0) badge.getStyleClass().add("hilo-positive");
            else if (countHint < 0) badge.getStyleClass().add("hilo-negative");
            else                    badge.getStyleClass().add("hilo-neutral");

            StackPane.setAlignment(badge, Pos.TOP_CENTER);
            StackPane.setMargin(badge, new Insets(5, 0, 0, 0));
            getChildren().add(badge);
        }

        setOpacity(0);
    }

    /** Convenience constructor — no hint badge. */
    public CardNode(Card card) {
        this(card, NO_HINT);
    }

    /** Plays the entrance animation — fade in with a slight upward slide. */
    public void reveal() {
        setTranslateY(20);
        FadeTransition fade = new FadeTransition(Duration.millis(350), this);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.millis(350), this);
        slide.setFromY(20);
        slide.setToY(0);
        slide.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scale = new ScaleTransition(Duration.millis(350), this);
        scale.setFromX(0.7);
        scale.setToX(1.0);
        scale.setFromY(0.7);
        scale.setToY(1.0);

        new ParallelTransition(fade, slide, scale).play();
    }

    public Card getCard() { return card; }
}
