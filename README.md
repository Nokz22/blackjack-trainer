# ♠ Blackjack Card Counting Trainer

> An educational desktop app to master the **Hi-Lo card counting system** — built with clean Java architecture and JavaFX.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![Maven](https://img.shields.io/badge/Maven-3.9-red?style=flat-square&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)
![Tests](https://img.shields.io/badge/Tests-JUnit%205-purple?style=flat-square)

---

## 📸 Preview

```
┌─────────────────────────────────────────────────────────┐
│  ♠ Blackjack Trainer          Level 3   Score: 1 450    │
├─────────────────────────────────────────────────────────┤
│       ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐          │
│       │  5   │  │  K   │  │  2   │  │  9   │          │
│       │  ♦   │  │  ♠   │  │  ♥   │  │  ♣   │          │
│       └──────┘  └──────┘  └──────┘  └──────┘          │
│              ✓ Correct!  +165 pts                       │
│     Running count:  [ -1  ]   [ Confirm ↵ ]            │
├─────────────────────────────────────────────────────────┤
│   Accuracy: 78.3%     Best Streak: 9     High Score: 2k │
└─────────────────────────────────────────────────────────┘
```

> _Add real screenshots to `/docs/screenshots/` and update paths here._

---

## ✨ Features

| Feature | Details |
|---|---|
| 🃏 **Card Counting** | Full Hi-Lo system (2–6 → +1, 7–9 → 0, 10/A → −1) |
| 🎮 **3 Game Modes** | Practice · Timed · Endless |
| 📈 **10 Levels** | Progressive speed, card count, and time pressure |
| 🏆 **Scoring System** | Base + level bonus + streak bonus + speed bonus |
| 💾 **Persistent Stats** | High score, best streak, total games (JSON file) |
| 🎨 **Polished UI** | Animated card reveals, feedback colours, green-felt theme |
| 🧪 **Unit Tested** | JUnit 5 tests for all core and game logic |
| 🔌 **Extensible** | Add new counting systems with one class |

---

## 🛠 Tech Stack

- **Java 21** — records, switch expressions, pattern matching
- **JavaFX 21** — Timeline, FadeTransition, ParallelTransition
- **Maven 3.9** — build, test, fat-JAR packaging
- **JUnit 5** — unit tests for pure logic layers
- **JSON (hand-crafted)** — zero-dependency stats persistence

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| JDK | 21+ |
| Maven | 3.9+ |

> JavaFX is pulled automatically by Maven — no manual SDK install needed.

### Run

```bash
git clone https://github.com/your-username/blackjack-trainer.git
cd blackjack-trainer
mvn javafx:run
```

### Test

```bash
mvn test
```

### Package (fat JAR)

```bash
mvn package
java -jar target/blackjack-trainer-1.0.0.jar
```

---

## 🏗 Architecture

The project enforces a strict three-layer separation. UI never touches core directly; core never knows JavaFX exists.

```
com.blackjacktrainer
├── core/               Pure domain — zero dependencies
│   ├── Card            Immutable card (Rank + Suit enums)
│   ├── Deck            Shuffled 1-8 deck shoe; auto-reshuffles
│   ├── Hand            Ordered list of dealt cards
│   ├── CountSystem     Strategy interface for counting systems
│   ├── HiLoCountSystem Hi-Lo implementation
│   └── GameState       Mutable session snapshot (score, level...)
│
├── game/               Orchestration — depends only on core
│   ├── GameMode        Enum: PRACTICE | TIMED | ENDLESS
│   ├── LevelConfig     Immutable config per level
│   ├── LevelManager    Progression table + advance logic
│   ├── ScoreManager    Points formula
│   ├── RoundResult     Immutable outcome of one round
│   ├── GameController  Central orchestrator
│   └── StatsPersistence Load/save to ~/.blackjack-trainer/stats.json
│
└── ui/                 JavaFX — depends on game layer only
    ├── Main            Application entry point
    ├── GameView        Scene graph + layout
    ├── UIController    Bridges FX events <-> GameController
    └── CardNode        Reusable animated card widget
```

### CountSystem — Strategy Pattern

Swapping Hi-Lo for any other system requires adding exactly one class:

```java
public class KnockoutCountSystem implements CountSystem {
    @Override
    public int valueFor(Card card) {
        return switch (card.getRank()) {
            case TWO, THREE, FOUR, FIVE, SIX, SEVEN -> +1;
            case EIGHT, NINE                         ->  0;
            case TEN, JACK, QUEEN, KING, ACE         -> -1;
        };
    }

    @Override
    public String getName() { return "KO (Knockout)"; }
}
```

No other class changes.

---

## 📁 Project Structure

```
blackjack-trainer/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── module-info.java
    │   │   └── com/blackjacktrainer/
    │   │       ├── app/       Main.java
    │   │       ├── core/      Card · Deck · Hand · CountSystem · HiLo · GameState
    │   │       ├── game/      GameController · LevelManager · ScoreManager · ...
    │   │       └── ui/        GameView · UIController · CardNode
    │   └── resources/
    │       └── css/style.css
    └── test/
        └── java/com/blackjacktrainer/
            ├── core/   HiLoCountSystemTest.java
            └── game/   ScoreManagerTest.java · LevelManagerTest.java
```

---

## 🎮 How to Play

1. Choose a mode on the menu screen.
2. Cards appear one by one — keep a running total in your head.
3. When all cards have been shown, type your running count and press Enter.
4. Green = correct · Red = incorrect · streak builds your score multiplier.
5. After **5 consecutive correct answers** you advance a level.
6. In **Endless mode** — one wrong answer ends the session.

### Hi-Lo Quick Reference

| Cards | Value |
|---|---|
| 2 · 3 · 4 · 5 · 6 | **+1** |
| 7 · 8 · 9 | **0** |
| 10 · J · Q · K · A | **−1** |

---

## 🗺 Roadmap

- [ ] Hi-Opt I and KO counting system support
- [ ] True Count (running ÷ remaining decks) toggle
- [ ] Sound effects (correct / wrong / level-up)
- [ ] Settings screen (number of decks, counting system)
- [ ] Full keyboard navigation
- [ ] Export stats to CSV
- [ ] Dark / light theme toggle
- [ ] Animated level-up celebration screen

---

## 📱 Mobile Migration Guide

This codebase was designed with portability in mind.

### What moves to Android unchanged

The entire `core/` and `game/` packages are **pure Java with zero framework dependencies**.

```
core/     ✅ Copy directly to Android / KMP
game/     ✅ Copy directly to Android / KMP
ui/       ❌ JavaFX — rewrite with Jetpack Compose
```

### Suggested Android ViewModel

```kotlin
class GameViewModel : ViewModel() {
    private val controller = GameController(GameMode.PRACTICE, HiLoCountSystem())

    fun startRound() = controller.startRound()

    fun submitAnswer(count: Int, elapsed: Double) =
        controller.submitAnswer(count, elapsed)
}
```

IntelliJ's **Code → Convert Java File to Kotlin** handles 90% of the conversion automatically. `record` types become `data class`, `switch` expressions remain identical.

---

## 🤝 Contributing

Pull requests welcome! Please:
- Follow the existing package structure
- Add tests for any new `core/` or `game/` logic
- Keep UI code in `ui/` — never let it leak into `core/`

---

## 📄 License

MIT © 2024
