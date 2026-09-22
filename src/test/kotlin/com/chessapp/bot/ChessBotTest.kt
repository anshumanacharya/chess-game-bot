package com.chessapp.bot

import com.chessapp.engine.Board
import com.chessapp.engine.CastlingRights
import com.chessapp.engine.ChessGame
import com.chessapp.engine.Color
import com.chessapp.engine.GameState
import com.chessapp.engine.MoveGenerator
import com.chessapp.engine.Piece
import com.chessapp.engine.PieceType
import com.chessapp.engine.Square
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChessBotTest {

    @Test
    fun `chooseMove returns null when there is no legal move`() {
        val board = Board.empty()
        board.setPiece(Square.fromAlgebraic("a8"), Piece(Color.BLACK, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("c7"), Piece(Color.WHITE, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("b6"), Piece(Color.WHITE, PieceType.QUEEN))
        val state = GameState(
            board = board,
            sideToMove = Color.BLACK,
            castlingRights = CastlingRights(false, false, false, false),
            enPassantTarget = null,
            halfMoveClock = 0,
            fullMoveNumber = 1
        )
        assertNull(ChessBot().chooseMove(state))
    }

    @Test
    fun `with noise and blunders disabled, plays the search's best move`() {
        val board = Board.empty()
        board.setPiece(Square.fromAlgebraic("a1"), Piece(Color.WHITE, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("h2"), Piece(Color.WHITE, PieceType.QUEEN))
        board.setPiece(Square.fromAlgebraic("a8"), Piece(Color.BLACK, PieceType.KING))
        board.setPiece(Square.fromAlgebraic("h8"), Piece(Color.BLACK, PieceType.ROOK))
        val state = GameState(
            board = board,
            sideToMove = Color.WHITE,
            castlingRights = CastlingRights(false, false, false, false),
            enPassantTarget = null,
            halfMoveClock = 0,
            fullMoveNumber = 1
        )
        val config = BotConfig(noiseCentipawns = 0, blunderProbability = 0.0)
        val move = ChessBot(config).chooseMove(state)
        assertNotNull(move)
        assertTrue(move.isCapture)
        assertEquals(Square.fromAlgebraic("h8"), move.to)
    }

    @Test
    fun `blunderProbability of 1 always plays a random legal move`() {
        val game = ChessGame()
        val config = BotConfig(blunderProbability = 1.0, random = Random(7))
        val move = ChessBot(config).chooseMove(game.state)
        assertNotNull(move)
        assertTrue(move in MoveGenerator.legalMoves(game.state))
    }

    @Test
    fun `bot only ever plays legal moves across a simulated game`() {
        val game = ChessGame()
        val bot = ChessBot(BotConfig.CASUAL.copy(random = Random(42)))
        var plies = 0
        while (plies < 30 && !game.status().isGameOver) {
            val move = bot.chooseMove(game.state) ?: break
            assertTrue(move in MoveGenerator.legalMoves(game.state))
            assertTrue(game.makeMove(move))
            plies++
        }
        assertTrue(plies > 0)
    }
}
