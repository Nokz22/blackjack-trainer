/**
 * ui.js — DOM rendering layer.
 *
 * Mirrors the Java ui/ package responsibility:
 * bridges game events to the HTML/CSS view.
 * No game logic lives here.
 */

'use strict';

// ── Screen routing ────────────────────────────────────────────────────────────

function showScreen(name) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById('screen-' + name).classList.add('active');
}

// ── HUD ───────────────────────────────────────────────────────────────────────

function updateHUD(state) {
  document.getElementById('hud-score').textContent  = state.score.toLocaleString();
  document.getElementById('hud-level').textContent  = state.level;
  document.getElementById('hud-streak').textContent = state.streak;

  const cfg = getLevelConfig(state.level);
  document.getElementById('level-badge').textContent =
    `${cfg.cards} cards · ${(cfg.delay / 1000).toFixed(1)}s`;

  const acc = getAccuracy(state);
  document.getElementById('hud-acc').textContent = acc === null ? '—' : acc + '%';

  document.getElementById('streak-label').textContent = streakLabel(state.streak);

  const streakEl = document.getElementById('hud-streak');
  streakEl.className = 'hud-val' + (state.streak >= 5 ? ' streak-fire' : '');
}

// ── Phase label ───────────────────────────────────────────────────────────────

function setPhase(text) {
  document.getElementById('phase-label').textContent = text;
}

// ── Feedback bar ──────────────────────────────────────────────────────────────

function showFeedback(correct, points, correctCount) {
  const el = document.getElementById('feedback-bar');
  if (correct) {
    el.innerHTML = `✓ Correct! <span class="feedback-points">+${points} pts</span>`;
    el.className = 'feedback-bar visible correct';
  } else {
    const sign = correctCount > 0 ? '+' : '';
    el.innerHTML = `✗ Wrong — the answer was <strong>${sign}${correctCount}</strong>`;
    el.className = 'feedback-bar visible wrong';
  }
}

function hideFeedback() {
  document.getElementById('feedback-bar').className = 'feedback-bar';
}

// ── Card stage ────────────────────────────────────────────────────────────────

function renderCards(hand) {
  const stage = document.getElementById('card-stage');
  stage.innerHTML = '';
  hand.forEach((card, i) => {
    const el = document.createElement('div');
    el.className = 'card ' + (isRed(card.suit) ? 'red' : 'black');
    el.id = 'card-' + i;
    const hv = hiLoValue(card.rank);
    el.innerHTML = `
      <span class="card-rank">${card.rank}</span>
      <span class="card-suit">${card.suit}</span>
      <span class="card-count-hint">${hv > 0 ? '+' : ''}${hv}</span>
    `;
    stage.appendChild(el);
  });
}

function revealCard(index) {
  const el = document.getElementById('card-' + index);
  if (el) el.classList.add('visible');
}

function revealAllCardHints() {
  document.querySelectorAll('.card').forEach(c => c.classList.add('revealed', 'visible'));
}

// ── Answer input ──────────────────────────────────────────────────────────────

function enableInput() {
  const input = document.getElementById('count-input');
  const btn   = document.getElementById('btn-confirm');
  input.disabled = false;
  btn.disabled   = false;
  input.value    = '';
  input.focus();
}

function disableInput() {
  document.getElementById('count-input').disabled = true;
  document.getElementById('btn-confirm').disabled  = true;
}

function getInputValue() {
  const raw = document.getElementById('count-input').value.trim();
  if (raw === '' || isNaN(raw)) return null;
  return parseInt(raw);
}

// ── Next / Quit buttons ───────────────────────────────────────────────────────

function showNextButton() {
  document.getElementById('btn-next').style.display = '';
}

function hideNextButton() {
  document.getElementById('btn-next').style.display = 'none';
}

// ── Level-up toast ────────────────────────────────────────────────────────────

function showLevelUp(level) {
  const t = document.getElementById('levelup-toast');
  t.textContent = `⬆ Level ${level}!`;
  t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 2000);
}

// ── Timer bar ─────────────────────────────────────────────────────────────────

let _timerInterval = null;

function startTimerBar(seconds, onExpire) {
  clearInterval(_timerInterval);
  const bar = document.getElementById('timer-bar');
  bar.style.width = '100%';
  bar.className = 'timer-bar';
  const end = Date.now() + seconds * 1000;

  _timerInterval = setInterval(() => {
    const remaining = Math.max(0, end - Date.now());
    const pct = remaining / (seconds * 1000) * 100;
    bar.style.width = pct + '%';
    if (pct < 25) bar.className = 'timer-bar urgent';
    if (remaining === 0) {
      clearInterval(_timerInterval);
      onExpire();
    }
  }, 80);
}

function stopTimerBar() {
  clearInterval(_timerInterval);
  const bar = document.getElementById('timer-bar');
  bar.style.width = '100%';
  bar.className = 'timer-bar';
}

// ── Game-over screen ──────────────────────────────────────────────────────────

function showGameOver(state, isEndless) {
  const title = document.getElementById('gameover-title');
  title.innerHTML = isEndless && state.totalRounds > 0
    ? 'Game <span>Over</span>'
    : 'Session <span>Complete</span>';

  const acc = getAccuracy(state);
  document.getElementById('go-score').textContent     = state.score.toLocaleString();
  document.getElementById('go-level').textContent     = state.level;
  document.getElementById('go-streak').textContent    = state.bestStreak;
  document.getElementById('go-acc').textContent       = (acc ?? 0) + '%';
  document.getElementById('go-rounds').textContent    = state.totalRounds;
  document.getElementById('go-highscore').textContent =
    Math.max(state.highScore, state.score).toLocaleString();

  showScreen('gameover');
}
