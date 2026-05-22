package app.weddingplanner.domain

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RsvpTokenTest {

    @Test
    fun tokenIsUrlSafe() {
        val token = RsvpToken.generate(Random(1))
        val allowed = ('A'..'Z') + ('a'..'z') + ('0'..'9') + listOf('-', '_')
        token.forEach { c ->
            assertTrue(c in allowed, "Otillåten tecken '$c' i token $token")
        }
    }

    @Test
    fun tokenIsAtLeastFortyTwoCharacters() {
        val token = RsvpToken.generate(Random(2))
        assertTrue(token.length >= 42, "Token för kort: ${token.length} tecken")
    }

    @Test
    fun tokenIsNotEqualWithDifferentRandom() {
        val a = RsvpToken.generate(Random(1))
        val b = RsvpToken.generate(Random(2))
        assertEquals(false, a == b)
    }
}
