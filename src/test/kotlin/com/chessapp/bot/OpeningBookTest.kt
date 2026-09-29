package com.chessapp.bot

import com.chessapp.engine.GameState
import com.chessapp.engine.MoveGenerator
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OpeningBookTest {

    private fun replay(uciLine: String): GameState {
        var state = GameState.newGame()
        if (uciLine.isBlank()) return state
        for (token in uciLine.split(' ')) {
            val move = MoveGenerator.legalMoves(state).firstOrNull { OpeningBook.uci(it) == token }
                ?: error("Book move $token is not legal after ${state.moveHistory.map { OpeningBook.uci(it) }}")
            state = MoveGenerator.applyMove(state, move)
        }
        return state
    }

    @Test
    fun `every bundled book line is legal move by move`() {
        for (chunk in OpeningBookData.CHUNKS) {
            for (line in chunk.lines().filter { it.isNotBlank() }) replay(line)
        }
    }

    @Test
    fun `picks a legal book move from the starting position`() {
        val state = GameState.newGame()
        val move = assertNotNull(OpeningBook.pick(state, Random(1)))
        assertTrue(move in MoveGenerator.legalMoves(state))
    }

    @Test
    fun `returns null for a position outside the book`() {
        val state = replay("a2a3 a7a6 a3a4 a6a5 h2h3 h7h6 h3h4 h6h5")
        assertNull(OpeningBook.pick(state, Random(1)))
    }

    @Test
    fun `weights favor moves that more opening lines continue with`() {
        val weights = OpeningBook.build(listOf("e2e4\ne2e4 e7e5\nd2d4"))[""]!!
        assertEquals(mapOf("e2e4" to 2, "d2d4" to 1), weights)
    }

    @Test
    fun `bot plays from the book inside the window and searches outside it`() {
        val start = GameState.newGame()
        val booked = ChessBot(BotConfig(bookMaxPlies = 4, bookDeviationProbability = 0.0, blunderProbability = 0.0, random = Random(3)))
        val move = assertNotNull(booked.chooseMove(start))
        assertTrue(OpeningBook.uci(move) in OpeningBook.build(OpeningBookData.CHUNKS).getValue(""))

        val noBook = ChessBot(BotConfig(bookMaxPlies = 0, blunderProbability = 0.0, noiseCentipawns = 0))
        assertNotNull(noBook.chooseMove(start))
    }

    @Test
    fun `ignores a custom position that merely has an empty move history`() {
        val board = com.chessapp.engine.Board.empty()
        board.setPiece(com.chessapp.engine.Square.fromAlgebraic("a1"), com.chessapp.engine.Piece(com.chessapp.engine.Color.WHITE, com.chessapp.engine.PieceType.KING))
        board.setPiece(com.chessapp.engine.Square.fromAlgebraic("h2"), com.chessapp.engine.Piece(com.chessapp.engine.Color.WHITE, com.chessapp.engine.PieceType.QUEEN))
        board.setPiece(com.chessapp.engine.Square.fromAlgebraic("a8"), com.chessapp.engine.Piece(com.chessapp.engine.Color.BLACK, com.chessapp.engine.PieceType.KING))
        val custom = GameState(
            board = board,
            sideToMove = com.chessapp.engine.Color.WHITE,
            castlingRights = com.chessapp.engine.CastlingRights(false, false, false, false),
            enPassantTarget = null,
            halfMoveClock = 0,
            fullMoveNumber = 1
        )
        assertNull(OpeningBook.pick(custom, Random(1)))
    }
}
