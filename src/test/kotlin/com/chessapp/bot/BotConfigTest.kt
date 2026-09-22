package com.chessapp.bot

import kotlin.test.Test
import kotlin.test.assertTrue

class BotConfigTest {

    @Test
    fun `presets get weaker in the expected order`() {
        assertTrue(BotConfig.STRONG.blunderProbability < BotConfig.CASUAL.blunderProbability)
        assertTrue(BotConfig.CASUAL.blunderProbability < BotConfig.BEGINNER.blunderProbability)
        assertTrue(BotConfig.STRONG.noiseCentipawns < BotConfig.CASUAL.noiseCentipawns)
        assertTrue(BotConfig.CASUAL.noiseCentipawns < BotConfig.BEGINNER.noiseCentipawns)
    }

    @Test
    fun `copy overrides only the field it targets`() {
        val custom = BotConfig.CASUAL.copy(noiseCentipawns = 200)
        assertTrue(custom.noiseCentipawns == 200)
        assertTrue(custom.blunderProbability == BotConfig.CASUAL.blunderProbability)
    }
}
