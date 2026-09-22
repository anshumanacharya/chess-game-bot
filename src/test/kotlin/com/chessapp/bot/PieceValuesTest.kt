package com.chessapp.bot

import com.chessapp.engine.PieceType
import kotlin.test.Test
import kotlin.test.assertEquals

class PieceValuesTest {

    @Test
    fun `standard values follow the usual pawn-unit convention`() {
        val values = PieceValues.STANDARD
        assertEquals(100, values.of(PieceType.PAWN))
        assertEquals(300, values.of(PieceType.KNIGHT))
        assertEquals(300, values.of(PieceType.BISHOP))
        assertEquals(500, values.of(PieceType.ROOK))
        assertEquals(900, values.of(PieceType.QUEEN))
        assertEquals(0, values.of(PieceType.KING))
    }

    @Test
    fun `overriding a single value leaves the rest at their defaults`() {
        val values = PieceValues(knight = 350)
        assertEquals(350, values.of(PieceType.KNIGHT))
        assertEquals(300, values.of(PieceType.BISHOP))
    }
}
