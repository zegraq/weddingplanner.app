package app.weddingplanner.api

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MockApiClientTest {
    @Test
    fun returnsHardcodedWedding() = runTest {
        val client = MockApiClient()
        val wedding = client.getWedding()
        assertEquals("2027-06-05", wedding.date)
        assertNull(wedding.venue)
    }
}
