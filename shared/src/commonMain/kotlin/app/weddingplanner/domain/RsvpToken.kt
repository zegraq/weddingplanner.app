package app.weddingplanner.domain

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.random.Random

object RsvpToken {
    private const val BYTE_LENGTH = 32

    @OptIn(ExperimentalEncodingApi::class)
    fun generate(random: Random = Random.Default): String {
        val bytes = random.nextBytes(BYTE_LENGTH)
        return Base64.UrlSafe.encode(bytes).trimEnd('=')
    }
}
