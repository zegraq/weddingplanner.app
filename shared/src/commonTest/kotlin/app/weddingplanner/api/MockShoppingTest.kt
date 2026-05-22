package app.weddingplanner.api

import app.weddingplanner.domain.Clock
import app.weddingplanner.domain.ShoppingItemInput
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockShoppingTest {

    private val fixedClock = Clock { "2026-05-22T10:00:00+02:00" }

    private fun client() = MockApiClient(clock = fixedClock, random = Random(42))

    @Test
    fun seedsShoppingItems() = runTest {
        val items = client().listShopping().getOrThrow()
        assertTrue(items.size >= 6, "Förväntade minst sex seedade inköpsposter")
        assertTrue(items.any { it.isBought }, "Förväntade en köpt post")
        assertTrue(items.any { !it.isBought }, "Förväntade en ej-köpt post")
        assertTrue(items.any { it.store == null }, "Förväntade en post utan butik")
    }

    @Test
    fun createAppendsItem() = runTest {
        val api = client()
        val before = api.listShopping().getOrThrow().size
        val created = api.createShopping(
            ShoppingItemInput(name = "Vit duk", quantity = 4, store = "IKEA", notes = null),
        ).getOrThrow()
        assertNull(created.boughtAt)
        assertEquals("Vit duk", created.name)
        assertEquals(4, created.quantity)
        assertEquals("IKEA", created.store)
        assertNotNull(created.createdAt)

        val after = api.listShopping().getOrThrow()
        assertEquals(before + 1, after.size)
    }

    @Test
    fun createRejectsBlankName() = runTest {
        val result = client().createShopping(
            ShoppingItemInput(name = "  ", quantity = 1, store = null, notes = null),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun createRejectsZeroQuantity() = runTest {
        val result = client().createShopping(
            ShoppingItemInput(name = "X", quantity = 0, store = null, notes = null),
        )
        assertTrue(result.isFailure)
    }

    @Test
    fun updateChangesFields() = runTest {
        val api = client()
        val target = api.listShopping().getOrThrow().first()
        val updated = api.updateShopping(
            target.id,
            ShoppingItemInput(
                name = "Servetter (vit)",
                quantity = 120,
                store = "Panduro",
                notes = "Bytt leverantör",
            ),
        ).getOrThrow()
        assertEquals("Servetter (vit)", updated.name)
        assertEquals(120, updated.quantity)
        assertEquals("Panduro", updated.store)
        assertEquals("Bytt leverantör", updated.notes)
        assertEquals(target.boughtAt, updated.boughtAt)
        assertEquals(target.createdAt, updated.createdAt)
    }

    @Test
    fun setBoughtAndUnset() = runTest {
        val api = client()
        val target = api.listShopping().getOrThrow().first { !it.isBought }
        val bought = api.setShoppingBought(target.id, "2026-05-22").getOrThrow()
        assertTrue(bought.isBought)
        assertEquals("2026-05-22", bought.boughtAt)

        val reset = api.setShoppingBought(target.id, null).getOrThrow()
        assertFalse(reset.isBought)
        assertNull(reset.boughtAt)
    }

    @Test
    fun setBoughtRejectsBadDate() = runTest {
        val api = client()
        val target = api.listShopping().getOrThrow().first()
        assertTrue(api.setShoppingBought(target.id, "imorgon").isFailure)
    }

    @Test
    fun deleteRemovesItem() = runTest {
        val api = client()
        val target = api.listShopping().getOrThrow().first()
        api.deleteShopping(target.id).getOrThrow()
        val remaining = api.listShopping().getOrThrow()
        assertFalse(remaining.any { it.id == target.id })
    }

    @Test
    fun unknownIdFailsCleanly() = runTest {
        val api = client()
        assertTrue(
            api.updateShopping(
                "sh-nonexistent",
                ShoppingItemInput("X", 1, null, null),
            ).isFailure,
        )
        assertTrue(api.setShoppingBought("sh-nonexistent", "2026-05-22").isFailure)
        assertTrue(api.deleteShopping("sh-nonexistent").isFailure)
    }
}
