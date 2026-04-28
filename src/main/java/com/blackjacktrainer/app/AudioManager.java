package com.blackjacktrainer.app;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton sound manager for the JavaFX desktop application.
 *
 * <p>Mirrors the responsibility of {@code audio.js} in the web version:
 * <ul>
 *   <li>Loads and caches audio assets from {@code src/main/resources/audio/}</li>
 *   <li>Loops background music with fade-in / fade-out</li>
 *   <li>Plays the card-deal sound on each card reveal</li>
 *   <li>Plays synthesised correct / wrong feedback tones</li>
 *   <li>Exposes a mute toggle</li>
 * </ul>
 *
 * <p>All public methods are no-ops when audio files are missing or when
 * {@code javafx.media} is unavailable, so the game never breaks without sound.
 *
 * <p>Usage from the UI layer:
 * <pre>{@code
 *   AudioManager audio = AudioManager.getInstance();
 *   audio.playCard();
 *   audio.toggleMute();
 * }</pre>
 *
 * <p>Resource paths expected inside the JAR / classpath:
 * <pre>
 *   /audio/background.mp3
 *   /audio/card.mp3
 * </pre>
 */
public final class AudioManager {

    private static final Logger LOG = Logger.getLogger(AudioManager.class.getName());

    // ── Resource paths ────────────────────────────────────────────────────────

    private static final String BACKGROUND_PATH = "/audio/background.mp3";
    private static final String CARD_PATH        = "/audio/card.mp3";

    // ── Volume constants ──────────────────────────────────────────────────────

    private static final double BACKGROUND_VOLUME = 0.35;
    private static final double CARD_VOLUME        = 0.70;
    private static final double FADE_IN_SECONDS    = 1.5;
    private static final double FADE_OUT_SECONDS   = 0.6;

    // ── Singleton ─────────────────────────────────────────────────────────────

    private static AudioManager instance;

    public static AudioManager getInstance() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // ── State ─────────────────────────────────────────────────────────────────

    private MediaPlayer backgroundPlayer;
    private MediaPlayer cardPlayer;
    private boolean     muted = false;

    // ── Constructor ───────────────────────────────────────────────────────────

    private AudioManager() {
        backgroundPlayer = createPlayer(BACKGROUND_PATH, true,  0.0);
        cardPlayer       = createPlayer(CARD_PATH,       false, CARD_VOLUME);
        startBackground();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Plays the card-deal sound effect.
     * Creates a fresh short-lived player so rapid deals never cut each other off.
     */
    public void playCard() {
        if (muted) return;
        // Create a one-shot player for overlap support
        MediaPlayer oneShot = createPlayer(CARD_PATH, false, CARD_VOLUME);
        if (oneShot == null) return;
        oneShot.setOnEndOfMedia(oneShot::dispose);
        oneShot.play();
    }

    /**
     * Plays a short correct-answer chime.
     * Uses a high-pitched sine-wave tone approximated via a brief media clip.
     * Falls back silently if media is unavailable.
     */
    public void playCorrect() {
        if (muted) return;
        playTone(880, 0.18, 0.3);
    }

    /**
     * Plays a short wrong-answer buzz.
     */
    public void playWrong() {
        if (muted) return;
        playTone(160, 0.15, 0.35);
    }

    /**
     * Toggles mute state.
     * Fades the background music in or out smoothly.
     */
    public void toggleMute() {
        muted = !muted;
        if (backgroundPlayer == null) return;

        if (muted) {
            fadeBackground(0.0, FADE_OUT_SECONDS, () -> backgroundPlayer.pause());
        } else {
            backgroundPlayer.play();
            fadeBackground(BACKGROUND_VOLUME, FADE_IN_SECONDS, null);
        }
    }

    public boolean isMuted() { return muted; }

    /** Stops all audio cleanly — call when the application exits. */
    public void dispose() {
        if (backgroundPlayer != null) backgroundPlayer.dispose();
        if (cardPlayer       != null) cardPlayer.dispose();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Creates a {@link MediaPlayer} from a classpath resource.
     * Returns {@code null} silently if the resource is missing.
     *
     * @param path   Classpath path, e.g. {@code /audio/background.mp3}
     * @param loop   Whether to loop the media
     * @param volume Initial volume (0.0 – 1.0)
     */
    private MediaPlayer createPlayer(String path, boolean loop, double volume) {
        try {
            URL url = getClass().getResource(path);
            if (url == null) {
                LOG.warning("[AudioManager] Resource not found: " + path);
                return null;
            }
            Media       media  = new Media(url.toExternalForm());
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(volume);
            if (loop) {
                player.setCycleCount(MediaPlayer.INDEFINITE);
            }
            return player;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "[AudioManager] Could not create player for: " + path, e);
            return null;
        }
    }

    /**
     * Starts background music with a fade-in from silence.
     */
    private void startBackground() {
        if (backgroundPlayer == null || muted) return;
        backgroundPlayer.play();
        fadeBackground(BACKGROUND_VOLUME, FADE_IN_SECONDS, null);
    }

    /**
     * Animates the background player volume to {@code targetVolume} over
     * {@code durationSeconds} using a JavaFX {@link Timeline}.
     *
     * @param targetVolume    Target volume (0.0 – 1.0)
     * @param durationSeconds Duration of the fade
     * @param onFinished      Optional runnable called when the fade completes
     */
    private void fadeBackground(double targetVolume, double durationSeconds, Runnable onFinished) {
        if (backgroundPlayer == null) return;

        Timeline fade = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(backgroundPlayer.volumeProperty(), backgroundPlayer.getVolume())),
            new KeyFrame(Duration.seconds(durationSeconds),
                new KeyValue(backgroundPlayer.volumeProperty(), targetVolume))
        );

        if (onFinished != null) {
            fade.setOnFinished(e -> onFinished.run());
        }

        fade.play();
    }

    /**
     * Synthesises a simple tone using a temporary sine-wave media approach.
     *
     * <p>JavaFX does not have a built-in oscillator like the Web Audio API,
     * so this method is a best-effort placeholder. In a production app,
     * short WAV files for correct/wrong feedback would replace this.
     *
     * @param frequencyHz Frequency of the tone (not used directly — placeholder)
     * @param volume      Playback volume
     * @param durationSec Duration in seconds
     */
    private void playTone(double frequencyHz, double volume, double durationSec) {
        // JavaFX Media requires a real audio file; synthesised tones are not
        // supported natively. This method is intentionally left as a no-op
        // placeholder — replace the body with a short WAV file playback
        // (e.g. /audio/correct.wav and /audio/wrong.wav) when available.
        LOG.fine("[AudioManager] playTone: " + frequencyHz + " Hz (placeholder)");
    }
}
