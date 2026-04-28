/**
 * audio.js — Sound manager for the web version.
 *
 * Mirrors the responsibility of AudioManager.java:
 *   - Loads and caches audio assets
 *   - Plays the card-deal sound on each card reveal
 *   - Loops background music with fade-in / fade-out
 *   - Exposes mute toggle (persisted in localStorage)
 *
 * All methods are no-ops if the browser blocks autoplay or if
 * the audio files are missing — the game never breaks without sound.
 *
 * Load order in index.html:
 *   core.js → audio.js → ui.js → game.js
 *
 * Called from game.js:
 *   AudioManager.init()        — once, on first user gesture (Start Game)
 *   AudioManager.playCard()    — every time a card is revealed
 *   AudioManager.playCorrect() — on correct answer
 *   AudioManager.playWrong()   — on wrong answer / timeout
 *   AudioManager.toggleMute()  — from the 🔊 navbar button
 */

'use strict';

const AudioManager = (() => {

    // ── Asset paths ───────────────────────────────────────────────────────────

    const BACKGROUND_SRC = 'audio/background.mp3';
    const CARD_SRC       = 'audio/card.mp3';

    // ── State ─────────────────────────────────────────────────────────────────

    let backgroundAudio = null;
    let cardAudio       = null;
    let muted           = localStorage.getItem('bj_muted') === 'true';
    let initialised     = false;

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Creates an HTMLAudioElement and returns it.
     * Returns null silently if the file cannot be loaded.
     */
    function createAudio(src, loop = false, volume = 1.0) {
        try {
            const audio  = new Audio(src);
            audio.loop   = loop;
            audio.volume = volume;
            return audio;
        } catch (e) {
            console.warn('[AudioManager] Could not load:', src, e);
            return null;
        }
    }

    /**
     * Smoothly fades the background music volume to a target value.
     *
     * @param {number}   targetVolume - 0.0 to 1.0
     * @param {number}   duration     - Milliseconds for the fade
     * @param {Function} [onDone]     - Optional callback when fade completes
     */
    function fadeTo(targetVolume, duration, onDone) {
        if (!backgroundAudio) return;

        const steps    = 20;
        const interval = duration / steps;
        const delta    = (targetVolume - backgroundAudio.volume) / steps;
        let   count    = 0;

        const tick = setInterval(() => {
            count++;
            backgroundAudio.volume = Math.min(1, Math.max(0,
                backgroundAudio.volume + delta
            ));
            if (count >= steps) {
                clearInterval(tick);
                backgroundAudio.volume = targetVolume;
                if (onDone) onDone();
            }
        }, interval);
    }

    /** Syncs the navbar mute button icon with the current mute state. */
    function updateMuteButton() {
        const btn = document.getElementById('nav-sound');
        if (btn) btn.textContent = muted ? '🔇' : '🔊';
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Initialises audio assets.
     * Must be called once after a user gesture (browser autoplay policy).
     * Safe to call multiple times — subsequent calls are no-ops.
     */
    function init() {
        if (initialised) return;
        initialised = true;

        backgroundAudio = createAudio(BACKGROUND_SRC, true, 0.0);
        cardAudio       = createAudio(CARD_SRC, false, 0.7);

        if (!muted && backgroundAudio) {
            backgroundAudio.play().catch(() => {
                // Autoplay blocked — will retry on next interaction
            });
            fadeTo(0.35, 1500);
        }

        updateMuteButton();
    }

    /**
     * Plays the card-deal sound effect.
     * Clones the Audio node so rapid deals don't cut each other off.
     */
    function playCard() {
        if (muted || !cardAudio) return;
        try {
            const clone = cardAudio.cloneNode();
            clone.volume = 0.7;
            clone.play().catch(() => {});
        } catch (e) {
            // Never crash the game due to audio
        }
    }

    /**
     * Plays a short correct-answer chime via Web Audio API.
     * No extra audio file required.
     */
    function playCorrect() {
        if (muted) return;
        try {
            const ctx  = new (window.AudioContext || window.webkitAudioContext)();
            const osc  = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.type            = 'sine';
            osc.frequency.value = 880;
            gain.gain.setValueAtTime(0.18, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.3);
            osc.start(ctx.currentTime);
            osc.stop(ctx.currentTime + 0.3);
        } catch (e) {}
    }

    /**
     * Plays a short wrong-answer buzz via Web Audio API.
     * No extra audio file required.
     */
    function playWrong() {
        if (muted) return;
        try {
            const ctx  = new (window.AudioContext || window.webkitAudioContext)();
            const osc  = ctx.createOscillator();
            const gain = ctx.createGain();
            osc.connect(gain);
            gain.connect(ctx.destination);
            osc.type            = 'sawtooth';
            osc.frequency.value = 160;
            gain.gain.setValueAtTime(0.15, ctx.currentTime);
            gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.35);
            osc.start(ctx.currentTime);
            osc.stop(ctx.currentTime + 0.35);
        } catch (e) {}
    }

    /**
     * Toggles mute on/off and persists the preference to localStorage.
     * Fades music in or out smoothly.
     */
    function toggleMute() {
        muted = !muted;
        localStorage.setItem('bj_muted', muted);

        if (backgroundAudio) {
            if (muted) {
                fadeTo(0.0, 600, () => backgroundAudio.pause());
            } else {
                backgroundAudio.play().catch(() => {});
                fadeTo(0.35, 800);
            }
        }

        updateMuteButton();
    }

    function isMuted() { return muted; }

    return { init, playCard, playCorrect, playWrong, toggleMute, isMuted };

})();
