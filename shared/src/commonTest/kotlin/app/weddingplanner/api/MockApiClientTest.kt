package app.weddingplanner.api

import app.weddingplanner.domain.Clock
import app.weddingplanner.domain.GuestInput
import app.weddingplanner.domain.HouseholdInput
import app.weddingplanner.domain.RsvpStatus
import kotlinx.coroutines.test.runTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class MockApiClientTest {

    private val fixedClock = Clock { "2026-05-22T10:00:00+02:00" }

    private fun client() = MockApiClient(clock = fixedClock, random = Random(42))

    @Test
    fun returnsHardcodedWedding() = runTest {
        val wedding = client().getWedding()
        assertEquals("2027-06-05", wedding.date)
        assertNull(wedding.venue)
    }

    @Test
    fun seedsExampleHouseholds() = runTest {
        val households = client().listHouseholds().getOrThrow()
        assertTrue(households.size >= 2, "Förväntade minst två seedade hushåll")
        households.forEach { household ->
            assertEquals(
                1,
                household.members.count { it.isMainContact },
                "Hushåll ${household.displayName} ska ha exakt en huvudansvarig",
            )
        }
    }

    @Test
    fun createsHouseholdWithGeneratedTokenAndIds() = runTest {
        val api = client()
        val before = api.listHouseholds().getOrThrow().size
        val input = HouseholdInput(
            displayName = "Bergmans",
            email = null,
            phone = null,
            tags = listOf("vänner"),
            notes = null,
            members = listOf(
                GuestInput("Erik Bergman", isMainContact = true, diet = null, notes = null),
                GuestInput("Anna Bergman", isMainContact = false, diet = "Vegan", notes = null),
            ),
        )
        val created = api.createHousehold(input).getOrThrow()
        assertEquals("Bergmans", created.displayName)
        assertEquals(2, created.members.size)
        assertTrue(created.rsvpToken.isNotBlank())
        assertNull(created.rsvpRespondedAt)
        created.members.forEach { assertEquals(RsvpStatus.Pending, it.rsvpStatus) }
        assertEquals(before + 1, api.listHouseholds().getOrThrow().size)
    }

    @Test
    fun rejectsHouseholdWithoutSingleMainContact() = runTest {
        val api = client()
        val noMain = HouseholdInput(
            displayName = "Trasigt",
            email = null,
            phone = null,
            tags = emptyList(),
            notes = null,
            members = listOf(GuestInput("Anon", isMainContact = false, diet = null, notes = null)),
        )
        assertTrue(api.createHousehold(noMain).isFailure)
    }

    @Test
    fun updatesHouseholdAndKeepsExistingMemberIds() = runTest {
        val api = client()
        val initial = api.listHouseholds().getOrThrow().first()
        val mainName = initial.members.first { it.isMainContact }.name
        val updated = api.updateHousehold(
            initial.id,
            HouseholdInput(
                displayName = "${initial.displayName} (uppdaterad)",
                email = "ny@example.com",
                phone = initial.phone,
                tags = initial.tags + "ny-tag",
                notes = "Uppdaterad anteckning",
                members = initial.members.map {
                    GuestInput(
                        name = it.name,
                        isMainContact = it.name == mainName,
                        diet = it.diet,
                        notes = it.notes,
                    )
                },
            ),
        ).getOrThrow()
        assertEquals("ny@example.com", updated.email)
        assertEquals("Uppdaterad anteckning", updated.notes)
        assertEquals(initial.members.map { it.id }, updated.members.map { it.id })
    }

    @Test
    fun deletesHousehold() = runTest {
        val api = client()
        val target = api.listHouseholds().getOrThrow().first()
        api.deleteHousehold(target.id).getOrThrow()
        val after = api.listHouseholds().getOrThrow()
        assertTrue(after.none { it.id == target.id })
    }

    @Test
    fun setsRsvpStatusAndRespondedAt() = runTest {
        val api = client()
        val target = api.listHouseholds().getOrThrow().first { it.rsvpRespondedAt == null }
        val guest = target.members.first()
        val updated = api.setGuestRsvpStatus(target.id, guest.id, RsvpStatus.Attending).getOrThrow()
        val updatedGuest = updated.members.first { it.id == guest.id }
        assertEquals(RsvpStatus.Attending, updatedGuest.rsvpStatus)
        assertNotNull(updated.rsvpRespondedAt)
        assertEquals("2026-05-22T10:00:00+02:00", updated.rsvpRespondedAt)
    }

    @Test
    fun unknownHouseholdReturnsFailure() = runTest {
        val api = client()
        val result = api.getHousehold("hh-nonexistent")
        if (result.isSuccess) fail("Förväntade Result.failure för okänt hushåll")
    }

    @Test
    fun tokensAreUniqueAcrossHouseholds() = runTest {
        val api = client()
        val tokens = api.listHouseholds().getOrThrow().map { it.rsvpToken }
        assertEquals(tokens.size, tokens.toSet().size, "Tokens måste vara unika")
        tokens.zipWithNext().forEach { (a, b) -> assertNotEquals(a, b) }
    }
}
