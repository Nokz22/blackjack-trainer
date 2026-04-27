/**
 * game.js — GameController orchestration + DOM event wiring.
 *
 * Mirrors GameController.java:
 *   - Deals rounds of cards from the shoe (Deck)
 *   - Validates player answers via HiLoCountSystem (runningCount)
 *   - Delegates scoring to ScoreManager (calculatePoints)
 *   - Delegates level advancement to LevelManager (shouldAdvance)
 *   - Updates GameState
 *
 * Key behaviours added in this revision:
 *   - PRACTICE mode: Hi-Lo badge revealed above each card as it is dealt
 *   - TIMED mode:    countdown timer starts after all cards are dealt;
 *                    time limit decreases with level (LevelConfig.timeLimit)
 *   - ENDLESS mode:  first wrong answer ends the session
 *
 * Depends on: core.js, ui.js  (loaded before this file in index.html)
 */

'use strict';

// ── Session-level variables ───────────────────────────────────────────────────

/** @type {object|null}  Current GameState (created by core.createGameState) */
let state = null;

/** @type {Array} Current shoe; reshuffle when it runs low */
let deck = [];

/** @type {Array} Cards dealt in the current round */
let currentHand = [];

/** @type {number} Timestamp (ms) when the answer input was enabled */
let roundStart = 0;

/** @type {number|null} setTimeout handle for the card-deal animation */
let dealTimeout = null;

/** @type {number} Index of the next card to reveal during deal animation */
let dealIdx = 0;

// ── Bootstrap ─────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {

  // Mode row selection (menu screen)
  document.querySelectorAll('.mode-row').forEach(row => {
    row.addEventListener('click', () => {
      document.querySelectorAll('.mode-row').forEach(r => r.classList.remove('selected'));
      row.classList.add('selected');
    });
  });

  // Menu buttons
  document.getElementById('btn-start').addEventListener('click', startGame);

  // Game screen buttons
  document.getElementById('btn-confirm').addEventListener('click', confirmAnswer);
  document.getElementById('btn-next').addEventListener('click', nextRound);
  document.getElementById('btn-quit').addEventListener('click', endGame);

  // Enter key submits answer
  document.getElementById('count-input').addEventListener('keydown', e => {
    if (e.key === 'Enter') confirmAnswer();
  });

  // Game-over screen buttons
  document.getElementById('btn-play-again').addEventListener('click', startGame);
  document.getElementById('btn-menu').addEventListener('click', () => {
    hideNavControls();
    showScreen('menu');
  });
});

// ── Game lifecycle ────────────────────────────────────────────────────────────

/**
 * Reads the selected mode from the menu, initialises a fresh GameState,
 * builds the shoe, and starts the first round.
 */
function startGame() {
  const selectedRow = document.querySelector('.mode-row.selected');
  const mode = selectedRow ? selectedRow.dataset.mode : 'PRACTICE';

  state = createGameState(mode);
  deck  = buildDeck(4);           // 4-deck shoe — mirrors Deck(4) in Java

  showNavControls();
  updateHUD(state);
  showScreen('game');
  startRound();
}

/**
 * Prepares a new round:
 *   1. Resets UI state
 *   2. Deals `cfg.cards` cards from the shoe
 *   3. Animates each card reveal with `cfg.delay` ms between cards
 */
function startRound() {
  if (!state.active) { endGame(); return; }

  hideFeedback();
  hideNextButton();
  disableInput();
  stopTimerBar();
  if (dealTimeout) clearTimeout(dealTimeout);

  const cfg        = getLevelConfig(state.level);
  const isPractice = state.mode === 'PRACTICE';

  // Reshuffle shoe when running low (mirrors Deck.deal() auto-reshuffle)
  if (deck.length < cfg.cards + 10) deck = buildDeck(4);

  currentHand = deck.splice(0, cfg.cards);

  // renderCards passes isPractice so ui.js knows whether to show badges
  renderCards(currentHand, isPractice);

  dealIdx = 0;
  scheduleDeal(cfg);
}

/**
 * Recursively reveals one card every `cfg.delay` ms.
 * Mirrors the JavaFX Timeline animation in UIController.
 *
 * @param {object} cfg - LevelConfig for the current level
 */
function scheduleDeal(cfg) {
  if (dealIdx < currentHand.length) {
    revealCard(dealIdx);   // ui.js — also shows badge if isPractice
    dealIdx++;
    dealTimeout = setTimeout(() => scheduleDeal(cfg), cfg.delay);
  } else {
    onAllCardsDealt(cfg);
  }
}

/**
 * Called when every card has been revealed.
 * Enables the answer input and starts the countdown timer in TIMED mode.
 *
 * @param {object} cfg - LevelConfig for the current level
 */
function onAllCardsDealt(cfg) {
  enableInput();
  roundStart = Date.now();

  // Timer runs in TIMED mode (and ENDLESS with a time limit) but NOT in PRACTICE
  const needsTimer = cfg.timeLimit > 0 && state.mode !== 'PRACTICE';
  if (needsTimer) {
    startTimerBar(cfg.timeLimit, onTimerExpired);
  }
}

// ── Answer handling ───────────────────────────────────────────────────────────

/**
 * Evaluates the player's answer.
 * Mirrors GameController.submitAnswer(int playerCount, double responseTimeSec).
 */
function confirmAnswer() {
  const playerCount = getInputValue();
  if (playerCount === null) return;   // empty / non-numeric input — ignore

  const correctCount    = runningCount(currentHand);   // HiLoCountSystem
  const correct         = playerCount === correctCount;
  const responseTimeSec = (Date.now() - roundStart) / 1000;

  stopTimerBar();
  revealAllBadges();   // show Hi-Lo hints on all cards after answer
  disableInput();

  let points  = 0;
  let levelUp = false;

  if (correct) {
    // ScoreManager.calculatePoints
    points = calculatePoints(state.level, state.streak, responseTimeSec);

    // Update GameState — mirrors GameState.recordCorrect() + incrementScore()
    state.score += points;
    state.totalRounds++;
    state.correctRounds++;
    state.streak++;
    if (state.streak > state.bestStreak) state.bestStreak = state.streak;
    if (state.score  > state.highScore)  state.highScore  = state.score;

    // LevelManager.shouldAdvance
    if (shouldAdvance(state.level, state.streak)) {
      state.level++;
      levelUp = true;
    }
  } else {
    // GameState.recordIncorrect()
    state.totalRounds++;
    state.streak = 0;
    if (state.mode === 'ENDLESS') state.active = false;
  }

  updateHUD(state);
  showFeedback(correct, points, correctCount);
  if (levelUp) setTimeout(() => showLevelUp(state.level), 300);

  if (!state.active) {
    setTimeout(endGame, 1400);
  } else {
    showNextButton();
  }
}

/**
 * Called when the TIMED mode countdown reaches zero.
 * Treated as an incorrect answer — mirrors the Java timer expiry logic.
 */
function onTimerExpired() {
  const correctCount = runningCount(currentHand);

  revealAllBadges();
  disableInput();

  // GameState.recordIncorrect()
  state.totalRounds++;
  state.streak = 0;
  if (state.mode === 'ENDLESS') state.active = false;

  updateHUD(state);
  showFeedback(false, 0, correctCount);

  if (!state.active) {
    setTimeout(endGame, 1200);
  } else {
    showNextButton();
  }
}

function nextRound() {
  startRound();
}

// ── Session end ───────────────────────────────────────────────────────────────

/**
 * Ends the current session:
 *   1. Clears any pending timeouts/intervals
 *   2. Persists high score and best streak (StatsPersistence)
 *   3. Shows the game-over screen
 */
function endGame() {
  if (dealTimeout) clearTimeout(dealTimeout);
  stopTimerBar();
  persistStats(state);                            // StatsPersistence.save()
  showGameOver(state, state.mode === 'ENDLESS');
}
