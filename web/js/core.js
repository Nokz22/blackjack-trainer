/**
 * core.js — Pure game logic, ported from Java.
 *
 * Mirrors the following Java classes exactly:
 *   Card, Deck, Hand, HiLoCountSystem, GameState,
 *   LevelConfig, LevelManager, ScoreManager
 *
 * Zero DOM dependencies — identical to the Java core/ package.
 */

'use strict';

// ── Card ──────────────────────────────────────────────────────────────────────

const RANKS = ['2','3','4','5','6','7','8','9','10','J','Q','K','A'];
const SUITS = ['♥','♦','♣','♠'];

function isRed(suit) {
  return suit === '♥' || suit === '♦';
}

// ── HiLoCountSystem ───────────────────────────────────────────────────────────

/**
 * Hi-Lo values:
 *   2–6  → +1
 *   7–9  →  0
 *   10–A → −1
 */
function hiLoValue(rank) {
  if (['2','3','4','5','6'].includes(rank)) return +1;
  if (['7','8','9'].includes(rank))         return  0;
  return -1;
}

function runningCount(hand) {
  return hand.reduce((sum, card) => sum + hiLoValue(card.rank), 0);
}

// ── Deck ──────────────────────────────────────────────────────────────────────

/**
 * Builds and shuffles a shoe of `numDecks` standard 52-card decks.
 * Returns an array; caller pops from the front (splice(0,n)).
 */
function buildDeck(numDecks = 4) {
  const deck = [];
  for (let d = 0; d < numDecks; d++) {
    for (const suit of SUITS) {
      for (const rank of RANKS) {
        deck.push({ rank, suit });
      }
    }
  }
  // Fisher-Yates shuffle
  for (let i = deck.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [deck[i], deck[j]] = [deck[j], deck[i]];
  }
  return deck;
}

// ── LevelManager ─────────────────────────────────────────────────────────────

const ROUNDS_TO_ADVANCE = 5;
const MAX_LEVEL = 10;

/**
 * Level config table — mirrors LevelManager.buildLevelTable() exactly.
 * Fields: level, cards (cardCount), delay (ms), timeLimit (s, 0 = none)
 */
const LEVELS = [
  { level:1,  cards:4,  delay:1800, timeLimit:0  },
  { level:2,  cards:5,  delay:1500, timeLimit:0  },
  { level:3,  cards:6,  delay:1300, timeLimit:0  },
  { level:4,  cards:6,  delay:1100, timeLimit:20 },
  { level:5,  cards:7,  delay:1000, timeLimit:18 },
  { level:6,  cards:7,  delay:900,  timeLimit:15 },
  { level:7,  cards:8,  delay:800,  timeLimit:12 },
  { level:8,  cards:8,  delay:700,  timeLimit:10 },
  { level:9,  cards:9,  delay:600,  timeLimit:8  },
  { level:10, cards:10, delay:500,  timeLimit:6  },
];

function getLevelConfig(level) {
  return LEVELS[Math.min(level, MAX_LEVEL) - 1];
}

function shouldAdvance(level, streak) {
  return level < MAX_LEVEL && streak > 0 && streak % ROUNDS_TO_ADVANCE === 0;
}

// ── ScoreManager ─────────────────────────────────────────────────────────────

/**
 * Points formula (mirrors ScoreManager.calculatePoints):
 *   base        = 100
 *   levelBonus  = level × 10
 *   streakBonus = min(streak × 5, 100)
 *   speedBonus  = max(0, 50 − responseTimeSec × 5)
 */
function calculatePoints(level, streak, responseTimeSec) {
  const levelBonus  = level * 10;
  const streakBonus = Math.min(streak * 5, 100);
  const speedBonus  = Math.max(0, Math.floor(50 - responseTimeSec * 5));
  return 100 + levelBonus + streakBonus + speedBonus;
}

function streakLabel(streak) {
  if (streak >= 20) return '🔥 LEGENDARY';
  if (streak >= 10) return '⚡ ON FIRE';
  if (streak >= 5)  return '✨ HOT STREAK';
  if (streak >= 3)  return '👍 NICE';
  return '';
}

// ── GameState ─────────────────────────────────────────────────────────────────

function createGameState(mode) {
  return {
    mode,
    score:         0,
    level:         1,
    streak:        0,
    totalRounds:   0,
    correctRounds: 0,
    highScore:     parseInt(localStorage.getItem('bj_highscore')   || '0'),
    bestStreak:    parseInt(localStorage.getItem('bj_beststreak')  || '0'),
    active:        true,
  };
}

function getAccuracy(state) {
  return state.totalRounds === 0
    ? null
    : Math.round(state.correctRounds / state.totalRounds * 100);
}

// ── StatsPersistence ─────────────────────────────────────────────────────────

function persistStats(state) {
  const storedHigh   = parseInt(localStorage.getItem('bj_highscore')  || '0');
  const storedStreak = parseInt(localStorage.getItem('bj_beststreak') || '0');
  if (state.score      > storedHigh)   localStorage.setItem('bj_highscore',  state.score);
  if (state.bestStreak > storedStreak) localStorage.setItem('bj_beststreak', state.bestStreak);
}
