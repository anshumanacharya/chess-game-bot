package com.chessapp.bot

import kotlin.random.Random

/**
 * Every knob that shapes how strong or human-like the bot plays, gathered in one place so it can
 * be tuned without touching [TwoPlySearch] or [ChessBot] at all. Start from one of the named
 * presets below and override individual fields with [copy], e.g. `BotConfig.CASUAL.copy(noiseCentipawns = 200)`.
 *
 * @property noiseCentipawns Random jitter (uniformly within ±this many centipawns) added to
 *   every candidate move's search score before comparing them, so the bot doesn't always play
 *   the objectively best move. Larger values mean weaker, more erratic play. 0 disables noise.
 * @property blunderProbability Chance, per move, that the bot ignores its search entirely and
 *   plays a uniformly random legal move instead — simulates an outright oversight, which noise
 *   alone rarely produces since noise still favors reasonable moves on average. Range 0.0–1.0.
 * @property pieceValues Centipawn value of each piece type, used by [MaterialEvaluator]. See
 *   [PieceValues] to experiment with different material weightings.
 * @property bookMaxPlies Number of half-moves (from the start of the game) for which the bot
 *   consults [OpeningBook] instead of searching. 0 disables the book.
 * @property bookDeviationProbability Chance, per move inside the book window, of skipping the
 *   book and searching normally — keeps weaker presets from sounding like opening theory.
 * @property random The source of randomness for noise, blunders, and blunder-move selection;
 *   inject a seeded `Random` for reproducible tests or replays.
 */
data class BotConfig(
    val noiseCentipawns: Int = 150,
    val blunderProbability: Double = 0.08,
    val pieceValues: PieceValues = PieceValues.STANDARD,
    val bookMaxPlies: Int = 8,
    val bookDeviationProbability: Double = 0.10,
    val random: Random = Random.Default
) {
    companion object {
        /** Plays close to its true best move almost every time: sharp, few oversights. */
        val STRONG = BotConfig(noiseCentipawns = 40, blunderProbability = 0.01, bookMaxPlies = 16, bookDeviationProbability = 0.0)

        /** The default. A rough ~1000 Elo design target (not a calibrated rating, since there's
         *  no rating engine to test against) — real mistakes on top of imperfect move choice. */
        val CASUAL = BotConfig(noiseCentipawns = 150, blunderProbability = 0.08)

        /** Frequent oversights and weak move choice, for a beginner-friendly opponent. */
        val BEGINNER = BotConfig(noiseCentipawns = 300, blunderProbability = 0.20, bookMaxPlies = 4, bookDeviationProbability = 0.30)
    }
}
