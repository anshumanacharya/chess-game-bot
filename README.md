# chess-game-bot

A chess-playing bot: given a position, it picks a move. This repo holds only the bot's
decision-making — move legality, check/checkmate detection, castling, en passant, etc. come from
[chess-game-](https://github.com/anshumanacharya/chess-game-)'s `chess-engine` library, pulled in
here as source (see [How it gets the rules](#how-it-gets-the-rules) below).

It's consumed by the [chess-game-](https://github.com/anshumanacharya/chess-game-) Android app
and web build, which always compile against whatever is on this repo's `main` branch — see
[How chess-game- stays in sync](#how-chess-game--stays-in-sync).

## How it works

Three small, independent pieces:

- **`TwoPlySearch`** — looks at every legal move, then one ply further at the opponent's best
  material-grabbing reply, and returns whichever of its own moves comes out best. This is what
  finds a free piece or an available mate. It's deterministic: same position in, same "best" move
  out, every time.
- **`Evaluator`** — scores a position from one side's perspective. The only implementation today,
  `MaterialEvaluator`, just adds up piece values. It's an interface so you can swap in something
  smarter (center control, king safety, ...) without touching the search.
- **`ChessBot`** — wraps `TwoPlySearch` with human-like imperfection: random noise added to each
  move's score before comparing them, and a small chance of ignoring the search entirely and
  playing a uniformly random legal move (a genuine blunder, which noise alone rarely produces).
  This is the class the game actually calls.

```
ChessBot.chooseMove(state)
  ├─ occasionally: a random legal move (BotConfig.blunderProbability)
  └─ otherwise: the TwoPlySearch move with the highest (score + random noise)
                 │
                 └─ TwoPlySearch.scoreMove(state, move)
                      └─ Evaluator.evaluate(board, color)   [MaterialEvaluator by default]
```

## Configuring it

Everything that shapes playing strength lives in one place, `BotConfig`:

```kotlin
data class BotConfig(
    val noiseCentipawns: Int = 150,       // how much randomness blurs the search's judgment
    val blunderProbability: Double = 0.08, // chance of an outright random move instead
    val pieceValues: PieceValues = PieceValues.STANDARD,
    val random: Random = Random.Default    // inject a seeded Random for reproducible tests
)
```

Three named presets to start from:

```kotlin
ChessBot(BotConfig.STRONG)    // noise 40, blunder 1%  — sharp, few oversights
ChessBot(BotConfig.CASUAL)    // noise 150, blunder 8% — the default; a rough ~1000 Elo
                               //   design target, not a calibrated rating (no rating engine to
                               //   test against)
ChessBot(BotConfig.BEGINNER)  // noise 300, blunder 20% — frequent, obvious mistakes
```

Override individual fields with `copy`:

```kotlin
ChessBot(BotConfig.CASUAL.copy(noiseCentipawns = 250))
```

To value pieces differently, pass custom `PieceValues`:

```kotlin
BotConfig.CASUAL.copy(pieceValues = PieceValues(knight = 320, bishop = 330))
```

## Extending it

- **A smarter evaluator** (e.g. rewarding center control or king safety): implement `Evaluator`
  and pass it to `TwoPlySearch(yourEvaluator)`. `ChessBot` doesn't need to change.
- **A deeper search**: `TwoPlySearch` isn't parameterized by depth today — write a new class with
  the same shape (`bestMove(state)` / `scoreMove(state, move)`) and swap it in wherever
  `ChessBot` constructs its search.
- **A new difficulty preset**: add a `val` to `BotConfig.Companion`, same shape as the existing
  three.

## How it gets the rules

The bot needs full legal-move generation to function, but chess rules already have one source of
truth in [chess-game-](https://github.com/anshumanacharya/chess-game-)'s `chess-engine` module —
duplicating castling/en passant/repetition logic here would just mean two copies to keep in sync
and twice the chance of a rules bug. So this repo pulls that source in directly rather than
reimplementing it: the `chess-engine-src` submodule is a full checkout of `chess-game-`, and
`build.gradle.kts` adds `chess-engine-src/chess-engine/src/main/kotlin` as an extra Kotlin source
directory — the exact same trick `chess-game-`'s own web build already uses to share that source
with its Kotlin/JS target. This repo's own code (everything under `src/`) only ever imports those
types; it never modifies them.

## How chess-game- stays in sync

`chess-game-` includes this repo as a git submodule and always builds against this repo's `main`
branch tip — not a pinned commit that has to be manually bumped. If you're working in
`chess-game-` and want your local changes here reflected there, just push to `main`; the next
build there picks it up automatically.

## Building and testing this repo standalone

```bash
git clone --recurse-submodules https://github.com/anshumanacharya/chess-game-bot
cd chess-game-bot
./gradlew test
```

If you already cloned without `--recurse-submodules`:

```bash
git submodule update --init
```
