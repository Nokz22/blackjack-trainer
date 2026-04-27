/**
 * game.js — GameController orchestration + event wiring.
 *
 * Mirrors GameController.java: deals rounds, validates answers,
 * delegates scoring/levelling, updates GameState.
 *
 * Depends on: core.js, ui.js
 */

'use strict';

// ── Session state ─────────────────────────────────────────────────────────────

let state       = null;
let deck        = [];
let currentHand = [];
let roundStart  = 0;
let dealTimeout = null;
let dealIdx     = 0;

// ── Bootstrap ─────────────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', () => {
  // Mode selection
  document.querySelectorAll('.mode-card').forEach(card => {
    card.addEventListener('click', () => {
      document.querySelectorAll('.mode-card').forEach(c => c.classList.remove('selected'));
      card.classList.add('selected');
    });
  });

  document.getElementById('btn-start').addEventListener('click', startGame);
  document.getElementById('btn-play-again').addEventListener('click', startGame);
  document.getElementById('btn-menu').addEventListener('click', () => showScreen('menu'));
  document.getElementById('btn-quit').addEventListener('click', endGame);
  document.getElementById('btn-confirm').addEventListener('click', confirmAnswer);
  document.getElementById('btn-next').addEventListener('click', nextRound);

  document.getElementById('count-input').addEventListener('keydown', e => {
    if (e.key === 'Enter') confirmAnswer();
  });
});

// ── Game lifecycle ────────────────────────────────────────────────────────────

function startGame() {
  const selectedCard = document.querySelector('.mode-card.selected');
  const mode = selectedCard ? selectedCard.dataset.mode : 'PRACTICE';

  state = createGameState(mode);
  deck  = buildDeck(4);

  updateHUD(state);
  showScreen('game');
  startRound();
}

function startRound() {
  if (!state.active) { endGame(); return; }

  hideFeedback();
  hideNextButton();
  disableInput();
  stopTimerBar();

  if (dealTimeout) clearTimeout(dealTimeout);

  const cfg = getLevelConfig(state.level);
  setPhase('Dealing cards...');

  // Reshuffle shoe when running low
  if (deck.length < cfg.cards + 10) deck = buildDeck(4);

  currentHand = deck.splice(0, cfg.cards);
  renderCards(currentHand);

  dealIdx = 0;
  scheduleDeal(cfg);
}

function scheduleDeal(cfg) {
  if (dealIdx < currentHand.length) {
    revealCard(dealIdx);
    dealIdx++;
    dealTimeout = setTimeout(() => scheduleDeal(cfg), cfg.delay);
  } else {
    onAllCardsDealt(cfg);
  }
}

function onAllCardsDealt(cfg) {
  setPhase('What is the running count?');
  enableInput();
  roundStart = Date.now();

  const needsTimer = cfg.timeLimit > 0 && state.mode !== 'PRACTICE';
  if (needsTimer) {
    startTimerBar(cfg.timeLimit, onTimerExpired);
  }
}

// ── Answer handling ───────────────────────────────────────────────────────────

function confirmAnswer() {
  const playerCount = getInputValue();
  if (playerCount === null) return;

  const correctCount   = runningCount(currentHand);
  const correct        = playerCount === correctCount;
  const responseTimeSec = (Date.now() - roundStart) / 1000;

  stopTimerBar();
  revealAllCardHints();
  disableInput();

  let points  = 0;
  let levelUp = false;

  if (correct) {
    points = calculatePoints(state.level, state.streak, responseTimeSec);
    state.score += points;
    state.totalRounds++;
    state.correctRounds++;
    state.streak++;
    if (state.streak > state.bestStreak) state.bestStreak = state.streak;
    if (state.score  > state.highScore)  state.highScore  = state.score;

    if (shouldAdvance(state.level, state.streak)) {
      state.level++;
      levelUp = true;
    }
  } else {
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

function onTimerExpired() {
  const correctCount = runningCount(currentHand);
  revealAllCardHints();
  disableInput();

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

function endGame() {
  if (dealTimeout) clearTimeout(dealTimeout);
  stopTimerBar();
  persistStats(state);
  showGameOver(state, state.mode === 'ENDLESS');
}
