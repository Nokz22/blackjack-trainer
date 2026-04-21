package com.blackjacktrainer.game;

import com.blackjacktrainer.core.GameState;

import java.io.*;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Persists player statistics to a local JSON file.
 *
 * <p>File location: {@code ~/.blackjack-trainer/stats.json}
 *
 * <p>Uses hand-crafted JSON serialisation to avoid adding a runtime
 * dependency for a simple two-field record. A production app would
 * use Jackson or Gson here.
 */
public class StatsPersistence {

    private static final Logger LOG = Logger.getLogger(StatsPersistence.class.getName());
    private static final String DIR  = System.getProperty("user.home") + File.separator + ".blackjack-trainer";
    private static final String FILE = DIR + File.separator + "stats.json";

    // ── Persistence record ────────────────────────────────────────────────────

    public record Stats(int highScore, int bestStreak, int totalGames) {
        public static Stats empty() { return new Stats(0, 0, 0); }
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Loads persisted stats, or returns empty defaults if no file exists. */
    public Stats load() {
        Path path = Path.of(FILE);
        if (!Files.exists(path)) return Stats.empty();
        try {
            String json = Files.readString(path);
            return parse(json);
        } catch (IOException | NumberFormatException e) {
            LOG.log(Level.WARNING, "Could not read stats file, using defaults.", e);
            return Stats.empty();
        }
    }

    /** Merges the session result with persisted stats and saves. */
    public void save(GameState sessionState) {
        Stats existing = load();
        Stats updated  = new Stats(
                Math.max(existing.highScore(), sessionState.getHighScore()),
                Math.max(existing.bestStreak(), sessionState.getBestStreak()),
                existing.totalGames() + 1
        );
        persist(updated);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void persist(Stats stats) {
        try {
            Files.createDirectories(Path.of(DIR));
            String json = String.format(
                    "{%n  \"highScore\": %d,%n  \"bestStreak\": %d,%n  \"totalGames\": %d%n}",
                    stats.highScore(), stats.bestStreak(), stats.totalGames());
            Files.writeString(Path.of(FILE), json);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Could not save stats.", e);
        }
    }

    /** Minimal hand-written JSON parser for our specific schema. */
    private Stats parse(String json) {
        int highScore  = extractInt(json, "highScore");
        int bestStreak = extractInt(json, "bestStreak");
        int totalGames = extractInt(json, "totalGames");
        return new Stats(highScore, bestStreak, totalGames);
    }

    private int extractInt(String json, String key) {
        String pattern = "\"" + key + "\"";
        int idx = json.indexOf(pattern);
        if (idx < 0) return 0;
        int colon = json.indexOf(':', idx);
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        return Integer.parseInt(json.substring(start, end));
    }
}
