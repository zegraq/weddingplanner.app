package app.weddingplanner.api

import app.weddingplanner.domain.Clock
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockBudgetTest {

    private val fixedClock = Clock { "2026-05-22T10:00:00+02:00" }

    private fun client() = MockApiClient(clock = fixedClock, random = Random(42))

    @Test
    fun seedsTotalBudgetAndCategories() = runTest {
        val view = client().getBudget().getOrThrow()
        assertEquals(350_000L, view.totalCap)
        assertTrue(view.categories.size >= 2, "Förväntade minst två seedade kategorier")
        val mat = view.categories.first { it.name == "Mat" }
        assertEquals(80_000L, mat.budgetedAmount)
        assertTrue(mat.items.any { it.isPaid }, "Förväntade en betald post i Mat-seed")
        assertTrue(mat.items.any { !it.isPaid }, "Förväntade en obetald post i Mat-seed")
    }

    @Test
    fun setsTotalBudgetAndReflectsInGetWedding() = runTest {
        val api = client()
        val updated = api.setTotalBudget(400_000L).getOrThrow()
        assertEquals(400_000L, updated.totalCap)
        val wedding = api.getWedding()
        assertEquals(400_000L, wedding.totalBudget)
    }

    @Test
    fun clearsTotalBudgetWithNull() = runTest {
        val api = client()
        val cleared = api.setTotalBudget(null).getOrThrow()
        assertNull(cleared.totalCap)
    }

    @Test
    fun rejectsNegativeTotalBudget() = runTest {
        val api = client()
        assertTrue(api.setTotalBudget(-1L).isFailure)
    }

    @Test
    fun createAndDeleteCategory() = runTest {
        val api = client()
        val before = api.getBudget().getOrThrow().categories.size
        val withNew = api.createCategory("Foto", 25_000L, notes = null).getOrThrow()
        assertEquals(before + 1, withNew.categories.size)
        val newCat = withNew.categories.last()
        assertEquals("Foto", newCat.name)
        assertEquals(25_000L, newCat.budgetedAmount)

        val afterDelete = api.deleteCategory(newCat.id).getOrThrow()
        assertEquals(before, afterDelete.categories.size)
    }

    @Test
    fun updateCategoryChangesNameAndBudget() = runTest {
        val api = client()
        val target = api.getBudget().getOrThrow().categories.first { it.name == "Mat" }
        val updated = api.updateCategory(target.id, "Mat & dryck", 95_000L, "Inkl. alkohol").getOrThrow()
        val cat = updated.categories.first { it.id == target.id }
        assertEquals("Mat & dryck", cat.name)
        assertEquals(95_000L, cat.budgetedAmount)
        assertEquals("Inkl. alkohol", cat.notes)
    }

    @Test
    fun addItemUpdateItemAndPayItem() = runTest {
        val api = client()
        val mat = api.getBudget().getOrThrow().categories.first { it.name == "Mat" }
        val view = api.addItem(mat.id, "Dryck", notes = null).getOrThrow()
        val newItem = view.categories.first { it.id == mat.id }.items.last()
        assertEquals("Dryck", newItem.description)
        assertFalse(newItem.isPaid)

        val renamed = api.updateItem(newItem.id, "Drycker (vin + öl)", "Bok via Systembolaget").getOrThrow()
        val updatedItem = renamed.categories
            .first { it.id == mat.id }.items
            .first { it.id == newItem.id }
        assertEquals("Drycker (vin + öl)", updatedItem.description)
        assertEquals("Bok via Systembolaget", updatedItem.notes)

        val paid = api.markItemPaid(newItem.id, 8_500L, "2027-03-01").getOrThrow()
        val paidItem = paid.categories
            .first { it.id == mat.id }.items
            .first { it.id == newItem.id }
        assertTrue(paidItem.isPaid)
        assertEquals(8_500L, paidItem.actualAmount)
        assertEquals("2027-03-01", paidItem.paidAt)
    }

    @Test
    fun markItemUnpaidClearsActualAndPaidAt() = runTest {
        val api = client()
        val mat = api.getBudget().getOrThrow().categories.first { it.name == "Mat" }
        val paidItem = mat.items.first { it.isPaid }
        val view = api.markItemUnpaid(paidItem.id).getOrThrow()
        val reverted = view.categories
            .first { it.id == mat.id }.items
            .first { it.id == paidItem.id }
        assertFalse(reverted.isPaid)
        assertNull(reverted.actualAmount)
        assertNull(reverted.paidAt)
    }

    @Test
    fun deleteItemRemovesItFromCategory() = runTest {
        val api = client()
        val mat = api.getBudget().getOrThrow().categories.first { it.name == "Mat" }
        val target = mat.items.first()
        val view = api.deleteItem(target.id).getOrThrow()
        val updatedMat = view.categories.first { it.id == mat.id }
        assertTrue(updatedMat.items.none { it.id == target.id })
    }

    @Test
    fun viewDerivesPlannedPaidAndUnallocated() = runTest {
        val api = client()
        api.setTotalBudget(300_000L).getOrThrow()
        val view = api.getBudget().getOrThrow()
        assertEquals(view.categories.sumOf { it.budgetedAmount }, view.plannedTotal)
        assertEquals(view.categories.sumOf { it.paidTotal }, view.paidTotal)
        assertEquals(300_000L - view.plannedTotal, view.unallocated)
    }

    @Test
    fun unknownCategoryFailsCleanly() = runTest {
        val api = client()
        assertTrue(api.deleteCategory("c-nonexistent").isFailure)
        assertTrue(api.updateCategory("c-nonexistent", "X", 0L, null).isFailure)
        assertTrue(api.addItem("c-nonexistent", "X", null).isFailure)
    }

    @Test
    fun unknownItemFailsCleanly() = runTest {
        val api = client()
        assertTrue(api.updateItem("bi-nonexistent", "X", null).isFailure)
        assertTrue(api.markItemPaid("bi-nonexistent", 0L, "2027-01-01").isFailure)
        assertTrue(api.deleteItem("bi-nonexistent").isFailure)
    }

    @Test
    fun isOverCapWhenPlannedExceedsTotal() = runTest {
        val api = client()
        api.setTotalBudget(50_000L).getOrThrow()
        val view = api.getBudget().getOrThrow()
        assertTrue(view.isOverCap, "Förväntade isOverCap=true när planerat överstiger taket")
        assertNotNull(view.unallocated)
        assertTrue(view.unallocated!! < 0)
    }
}
