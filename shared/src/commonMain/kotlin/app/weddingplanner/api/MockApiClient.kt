package app.weddingplanner.api

import app.weddingplanner.domain.Clock
import app.weddingplanner.domain.Guest
import app.weddingplanner.domain.GuestInput
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.HouseholdInput
import app.weddingplanner.domain.RsvpStatus
import app.weddingplanner.domain.RsvpToken
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MockApiClient(
    private val clock: Clock,
    private val random: Random = Random.Default,
) : ApiClient {

    private val mutex = Mutex()
    private val households = mutableListOf<Household>()
    private var idCounter = 0L

    init {
        seed()
    }

    override suspend fun getWedding(): Wedding = Wedding(
        date = "2027-06-05",
        venue = null,
    )

    override suspend fun listHouseholds(): Result<List<Household>> = runCatching {
        mutex.withLock { households.toList() }
    }

    override suspend fun getHousehold(id: String): Result<Household> = runCatching {
        mutex.withLock {
            households.firstOrNull { it.id == id }
                ?: error("Hushåll $id finns inte")
        }
    }

    override suspend fun createHousehold(input: HouseholdInput): Result<Household> = runCatching {
        mutex.withLock {
            require(input.members.isNotEmpty()) { "Hushållet måste ha minst en medlem" }
            require(input.members.count { it.isMainContact } == 1) {
                "Exakt en medlem måste vara huvudansvarig"
            }
            val householdId = nextId("hh")
            val members = input.members.map { it.toGuest(householdId) }
            val household = Household(
                id = householdId,
                displayName = input.displayName,
                email = input.email,
                phone = input.phone,
                tags = input.tags,
                rsvpToken = RsvpToken.generate(random),
                rsvpRespondedAt = null,
                notes = input.notes,
                members = members,
            )
            households += household
            household
        }
    }

    override suspend fun updateHousehold(
        id: String,
        input: HouseholdInput,
    ): Result<Household> = runCatching {
        mutex.withLock {
            require(input.members.isNotEmpty()) { "Hushållet måste ha minst en medlem" }
            require(input.members.count { it.isMainContact } == 1) {
                "Exakt en medlem måste vara huvudansvarig"
            }
            val index = households.indexOfFirst { it.id == id }
            require(index >= 0) { "Hushåll $id finns inte" }
            val existing = households[index]
            val existingByName = existing.members.associateBy { it.name }
            val mergedMembers = input.members.map { inputMember ->
                val carry = existingByName[inputMember.name]
                if (carry != null) {
                    carry.copy(
                        isMainContact = inputMember.isMainContact,
                        diet = inputMember.diet,
                        notes = inputMember.notes,
                    )
                } else {
                    inputMember.toGuest(existing.id)
                }
            }
            val updated = existing.copy(
                displayName = input.displayName,
                email = input.email,
                phone = input.phone,
                tags = input.tags,
                notes = input.notes,
                members = mergedMembers,
            )
            households[index] = updated
            updated
        }
    }

    override suspend fun deleteHousehold(id: String): Result<Unit> = runCatching {
        mutex.withLock {
            val removed = households.removeAll { it.id == id }
            require(removed) { "Hushåll $id finns inte" }
        }
    }

    override suspend fun setGuestRsvpStatus(
        householdId: String,
        guestId: String,
        status: RsvpStatus,
    ): Result<Household> = runCatching {
        mutex.withLock {
            val index = households.indexOfFirst { it.id == householdId }
            require(index >= 0) { "Hushåll $householdId finns inte" }
            val existing = households[index]
            val updatedMembers = existing.members.map { member ->
                if (member.id == guestId) member.copy(rsvpStatus = status) else member
            }
            require(updatedMembers != existing.members) { "Gäst $guestId finns inte i hushållet" }
            val anyResponded = updatedMembers.any { it.rsvpStatus != RsvpStatus.Pending }
            val updated = existing.copy(
                members = updatedMembers,
                rsvpRespondedAt = if (anyResponded) clock.nowIso() else null,
            )
            households[index] = updated
            updated
        }
    }

    private fun nextId(prefix: String): String {
        idCounter += 1
        return "$prefix-${idCounter.toString(16).padStart(4, '0')}"
    }

    private fun GuestInput.toGuest(householdId: String): Guest = Guest(
        id = nextId("g"),
        householdId = householdId,
        name = name,
        isMainContact = isMainContact,
        rsvpStatus = RsvpStatus.Pending,
        diet = diet,
        notes = notes,
    )

    private fun seed() {
        val anderssonId = nextId("hh")
        val anderssonMembers = listOf(
            Guest(nextId("g"), anderssonId, "Maria Andersson", true, RsvpStatus.Attending, null, null),
            Guest(nextId("g"), anderssonId, "Lars Andersson", false, RsvpStatus.Attending, "Glutenfri", null),
            Guest(nextId("g"), anderssonId, "Elin Andersson", false, RsvpStatus.Pending, null, "8 år"),
        )
        households += Household(
            id = anderssonId,
            displayName = "Familjen Andersson",
            email = "maria@andersson.example",
            phone = null,
            tags = listOf("familj"),
            rsvpToken = RsvpToken.generate(random),
            rsvpRespondedAt = clock.nowIso(),
            notes = null,
            members = anderssonMembers,
        )

        val ericssonId = nextId("hh")
        val ericssonMembers = listOf(
            Guest(nextId("g"), ericssonId, "Sara Ericsson", true, RsvpStatus.Pending, null, null),
        )
        households += Household(
            id = ericssonId,
            displayName = "Sara Ericsson",
            email = "sara@jobbet.example",
            phone = "+46701234567",
            tags = listOf("Sara-jobb"),
            rsvpToken = RsvpToken.generate(random),
            rsvpRespondedAt = null,
            notes = "Jobbar med Sara på Tetra",
            members = ericssonMembers,
        )
    }
}
