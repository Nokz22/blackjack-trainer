/**
 * ui.js — DOM rendering layer.
 *
 * Mirrors the responsibility of the Java ui/ package:
 * bridges game events to the HTML/CSS view. No game logic lives here.
 *
 * Notable additions vs. original:
 *   - renderCards() accepts isPractice flag → shows Hi-Lo badge above each card
 *   - startTimerBar() animates the timer fill and shows remaining seconds
 *   - updateStatsBar() keeps the bottom stats bar in sync
 *
 * Depends on: core.js (hiLoValue, isRed, getLevelConfig, getAccuracy, streakLabel)
 */

'use strict';

// ── Screen routing ────────────────────────────────────────────────────────────

function showScreen(name) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById('screen-' + name).classList.add('active');
}

// ── Navbar ────────────────────────────────────────────────────────────────────

/**
 * Shows the in-game navbar controls (level pill, score pill, quit button).
 * Called once when a game session starts.
 */
function showNavControls() {
  document.getElementById('nav-controls').style.display = 'flex';
}

function hideNavControls() {
  document.getElementById('nav-controls').style.display = 'none';
}

// ── HUD + Stats bar ───────────────────────────────────────────────────────────

/**
 * Syncs the navbar pills and bottom stats bar from the current GameState.
 *
 * @param {object} state - GameState object from core.js
 */
function updateHUD(state) {
  // Navbar pills
  document.getElementById('nav-level').textContent = 'Level ' + state.level;
  document.getElementById('nav-score').textContent = 'Score: ' + state.score.toLocaleString();

  // Bottom stats bar
  updateStatsBar(state);
}

function updateStatsBar(state) {
  const acc = getAccuracy(state);
  document.getElementById('sb-acc').textContent    = 'Accuracy: ' + (acc === null ? '—' : acc + '%');
  document.getElementById('sb-streak').textContent = 'Best Streak: ' + state.bestStreak;
  document.getElementById('sb-high').textContent   = 'High Score: ' + Math.max(state.highScore, state.score).toLocaleString();
}

// ── Card stage ────────────────────────────────────────────────────────────────

/**
 * Renders all cards in the stage as hidden .card-wrap elements.
 * Each wrap contains an optional Hi-Lo badge (visible in Practice mode)
 * and the card face.
 *
 * Badges are hidden initially and revealed via revealCard() or revealAllBadges().
 *
 * @param {Array}   hand       - Array of { rank, suit } card objects
 * @param {boolean} isPractice - True in PRACTICE mode → badges shown on reveal
 */
function renderCards(hand, isPractice) {
  const stage = document.getElementById('card-stage');
  stage.innerHTML = '';

  hand.forEach((card, i) => {
    const hv = hiLoValue(card.rank);

    // Badge element (always created; only shown in practice mode)
    const badge = document.createElement('div');
    badge.className = 'card-badge ' + (hv > 0 ? 'pos' : hv < 0 ? 'neg' : 'neu');
    badge.textContent = hv > 0 ? '+1' : hv < 0 ? '−1' : '0';
    badge.dataset.practice = isPractice ? 'true' : 'false';

    // Card face
    const cardEl = document.createElement('div');
    cardEl.className = 'card ' + (isRed(card.suit) ? 'red' : 'black');
    cardEl.innerHTML =
      '<span class="card-rank">' + card.rank + '</span>' +
      '<span class="card-suit">' + card.suit + '</span>';

    // Wrapper: badge on top, card below
    const wrap = document.createElement('div');
    wrap.className = 'card-wrap';
    wrap.id = 'card-wrap-' + i;
    wrap.appendChild(badge);
    wrap.appendChild(cardEl);

    stage.appendChild(wrap);
  });
}

/**
 * Reveals the card at the given index (fade-in animation).
 * In PRACTICE mode, also shows the Hi-Lo badge above the card.
 *
 * @param {number} index - Zero-based card index
 */
function revealCard(index) {
  const wrap = document.getElementById('card-wrap-' + index);
  if (!wrap) return;

  wrap.classList.add('visible');

  // Show badge only in Practice mode
  const badge = wrap.querySelector('.card-badge');
  if (badge && badge.dataset.practice === 'true') {
    // Slight delay so the card appears first, then badge fades in
    setTimeout(() => badge.classList.add('show'), 120);
  }
}

/**
 * Reveals all card badges — called after the player submits an answer
 * in non-practice modes, so they can review which cards affected the count.
 */
function revealAllBadges() {
  document.querySelectorAll('.card-badge').forEach(b => b.classList.add('show'));
  document.querySelectorAll('.card-wrap').forEach(w => w.classList.add('visible'));
}

// ── Feedback row ──────────────────────────────────────────────────────────────

/**
 * @param {boolean} correct
 * @param {number}  points       - Points awarded (only shown if correct)
 * @param {number}  correctCount - The actual running count
 */
function showFeedback(correct, points, correctCount) {
  const el = document.getElementById('feedback-row');
  if (correct) {
    el.innerHTML =
      '✓ Correct! <span class="feedback-points">+' + points + ' pts</span>';
    el.className = 'feedback-row visible correct';
  } else {
    const sign = correctCount > 0 ? '+' : '';
    el.innerHTML =
      '✗ Wrong — the answer was <strong>' + sign + correctCount + '</strong>';
    el.className = 'feedback-row visible wrong';
  }
}

function hideFeedback() {
  document.getElementById('feedback-row').className = 'feedback-row';
  document.getElementById('feedback-row').innerHTML = '';
}

// ── Answer input ──────────────────────────────────────────────────────────────

function enableInput() {
  const input = document.getElementById('count-input');
  input.disabled = false;
  input.value    = '';
  document.getElementById('btn-confirm').disabled = false;
  input.focus();
}

function disableInput() {
  document.getElementById('count-input').disabled = true;
  document.getElementById('btn-confirm').disabled = true;
}

/** @returns {number|null} Parsed integer or null if input is empty/invalid */
function getInputValue() {
  const raw = document.getElementById('count-input').value.trim();
  if (raw === '' || isNaN(raw)) return null;
  return parseInt(raw, 10);
}

// ── Next round button ─────────────────────────────────────────────────────────

function showNextButton() {
  document.getElementById('next-row').style.display = '';
}

function hideNextButton() {
  document.getElementById('next-row').style.display = 'none';
}

// ── Timer bar (TIMED mode only) ───────────────────────────────────────────────

let _timerInterval = null;

/**
 * Animates the timer fill bar from 100% → 0% over `seconds`.
 * Calls `onExpire` when time runs out.
 * Updates the numeric label every second.
 *
 * @param {number}   seconds  - Total allowed time
 * @param {Function} onExpire - Called when the timer reaches zero
 */
function startTimerBar(seconds, onExpire) {
  clearInterval(_timerInterval);

  const fill  = document.getElementById('timer-fill');
  const label = document.getElementById('timer-label');
  const wrap  = document.getElementById('timer-wrap');

  wrap.style.display = '';
  fill.style.width   = '100%';
  fill.className     = 'timer-fill';
  label.className    = 'timer-label';
  label.textContent  = seconds + 's';

  const end = Date.now() + seconds * 1000;

  _timerInterval = setInterval(() => {
    const remaining = Math.max(0, end - Date.now());
    const pct       = (remaining / (seconds * 1000)) * 100;
    const secs      = Math.ceil(remaining / 1000);

    fill.style.width  = pct + '%';
    label.textContent = secs + 's';

    const isUrgent = pct < 25;
    fill.className  = 'timer-fill'  + (isUrgent ? ' urgent' : '');
    label.className = 'timer-label' + (isUrgent ? ' urgent' : '');

    if (remaining === 0) {
      clearInterval(_timerInterval);
      onExpire();
    }
  }, 80);
}

function stopTimerBar() {
  clearInterval(_timerInterval);
  const fill  = document.getElementById('timer-fill');
  const label = document.getElementById('timer-label');
  const wrap  = document.getElementById('timer-wrap');
  if (fill)  { fill.style.width = '100%'; fill.className = 'timer-fill'; }
  if (label) { label.className = 'timer-label'; }
  if (wrap)  { wrap.style.display = 'none'; }
}

// ── Level-up toast ────────────────────────────────────────────────────────────

function showLevelUp(level) {
  const toast = document.getElementById('levelup-toast');
  toast.textContent = '⬆ Level ' + level + '!';
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2000);
}

// ── Game-over screen ──────────────────────────────────────────────────────────

/**
 * Populates and shows the game-over screen.
 *
 * @param {object}  state     - Final GameState
 * @param {boolean} isEndless - True if session ended due to Endless mode error
 */
function showGameOver(state, isEndless) {
  const title = document.getElementById('gameover-title');
  title.textContent = (isEndless && state.totalRounds > 0)
    ? 'Game Over'
    : 'Session Complete';

  const acc = getAccuracy(state);
  document.getElementById('go-score').textContent     = state.score.toLocaleString();
  document.getElementById('go-level').textContent     = state.level;
  document.getElementById('go-streak').textContent    = state.bestStreak;
  document.getElementById('go-acc').textContent       = (acc ?? 0) + '%';
  document.getElementById('go-rounds').textContent    = state.totalRounds;
  document.getElementById('go-highscore').textContent =
    Math.max(state.highScore, state.score).toLocaleString();

  hideNavControls();
  showScreen('gameover');
}
