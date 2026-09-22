package com.chessapp.bot

import com.chessapp.engine.GameState
import com.chessapp.engine.Move
import com.chessapp.engine.MoveGenerator

/**
 * The bot's public entry point. Pass a [BotConfig] (or use one of its presets — [BotConfig.CASUAL]
 * by default) and call [chooseMove] once per turn.
 *
 * This class only wires the pieces together; it holds no chess-playing logic of its own:
 * - [TwoPlySearch] finds the objectively best move it can see two plies deep.
 * - [BotConfig.noiseCentipawns] and [BotConfig.blunderProbability] are layered on top here, so
 *   the bot doesn't always play that objectively best move.
 *
 * To change how *strong* the bot plays, adjust [BotConfig]. To change how it *thinks* (e.g. a
 * deeper search, or an evaluator that considers more than material), replace [TwoPlySearch] or
 * [Evaluator] instead — [ChessBot] itself shouldn't need to change either way.
 */
class ChessBot(private val config: BotConfig = BotConfig.CASUAL) {

    private val search = TwoPlySearch(MaterialEvaluator(config.pieceValues))

    /** The bot's chosen move, or null if the side to move has no legal move (game already over). */
    fun chooseMove(state: GameState): Move? {
        val legalMoves = MoveGenerator.legalMoves(state)
        if (legalMoves.isEmpty()) return null

        if (config.random.nextDouble() < config.blunderProbability) {
            return legalMoves.random(config.random)
        }

        var bestMove = legalMoves.first()
        var bestScore = Int.MIN_VALUE
        for (move in legalMoves) {
            val score = search.scoreMove(state, move) + noise()
            if (score > bestScore) {
                bestScore = score
                bestMove = move
            }
        }
        return bestMove
    }

    private fun noise(): Int {
        val n = config.noiseCentipawns
        return if (n <= 0) 0 else config.random.nextInt(-n, n + 1)
    }
}
