package app.weddingplanner.api

import app.weddingplanner.domain.BudgetCategory
import app.weddingplanner.domain.BudgetItem
import app.weddingplanner.domain.BudgetView
import app.weddingplanner.domain.Clock
import app.weddingplanner.domain.Guest
import app.weddingplanner.domain.GuestInput
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.HouseholdInput
import app.weddingplanner.domain.RsvpStatus
import app.weddingplanner.domain.RsvpToken
import app.weddingplanner.domain.TodoAssignee
import app.weddingplanner.domain.TodoInput
import app.weddingplanner.domain.TodoItem
import app.weddingplanner.domain.TodoStatus
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class MockApiClient(
    private val clock: Clock,
    private val random: Random = Random.Default,
) : ApiClient {

    private val mutex = Mutex()
    private val households = mutableListOf<Household>()
    private val categories = mutableListOf<BudgetCategory>()
    private val todos = mutableListOf<TodoItem>()
    private var totalBudget: Long? = null
    private var wedding: Wedding = Wedding(date = "2027-06-05", venue = null, totalBudget = null)
    private var idCounter = 0L

    init {
        seed()
        seedBudget()
        seedTodos()
    }

    override suspend fun getWedding(): Wedding = mutex.withLock { wedding }

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

    override suspend fun getBudget(): Result<BudgetView> = runCatching {
        mutex.withLock { snapshotBudget() }
    }

    override suspend fun setTotalBudget(amount: Long?): Result<BudgetView> = runCatching {
        mutex.withLock {
            require(amount == null || amount >= 0) { "Totalbudget kan inte vara negativ" }
            totalBudget = amount
            wedding = wedding.copy(totalBudget = amount)
            snapshotBudget()
        }
    }

    override suspend fun createCategory(
        name: String,
        budgetedAmount: Long,
        notes: String?,
    ): Result<BudgetView> = runCatching {
        mutex.withLock {
            require(name.isNotBlank()) { "Kategorinamn krävs" }
            require(budgetedAmount >= 0) { "Budgetbelopp kan inte vara negativt" }
            categories += BudgetCategory(
                id = nextId("c"),
                name = name.trim(),
                budgetedAmount = budgetedAmount,
                notes = notes?.trim()?.ifBlank { null },
                items = emptyList(),
            )
            snapshotBudget()
        }
    }

    override suspend fun updateCategory(
        id: String,
        name: String,
        budgetedAmount: Long,
        notes: String?,
    ): Result<BudgetView> = runCatching {
        mutex.withLock {
            require(name.isNotBlank()) { "Kategorinamn krävs" }
            require(budgetedAmount >= 0) { "Budgetbelopp kan inte vara negativt" }
            val index = categories.indexOfFirst { it.id == id }
            require(index >= 0) { "Kategori $id finns inte" }
            categories[index] = categories[index].copy(
                name = name.trim(),
                budgetedAmount = budgetedAmount,
                notes = notes?.trim()?.ifBlank { null },
            )
            snapshotBudget()
        }
    }

    override suspend fun deleteCategory(id: String): Result<BudgetView> = runCatching {
        mutex.withLock {
            val removed = categories.removeAll { it.id == id }
            require(removed) { "Kategori $id finns inte" }
            snapshotBudget()
        }
    }

    override suspend fun addItem(
        categoryId: String,
        description: String,
        notes: String?,
    ): Result<BudgetView> = runCatching {
        mutex.withLock {
            require(description.isNotBlank()) { "Beskrivning krävs" }
            val index = categories.indexOfFirst { it.id == categoryId }
            require(index >= 0) { "Kategori $categoryId finns inte" }
            val item = BudgetItem(
                id = nextId("bi"),
                categoryId = categoryId,
                description = description.trim(),
                actualAmount = null,
                paidAt = null,
                notes = notes?.trim()?.ifBlank { null },
            )
            categories[index] = categories[index].copy(items = categories[index].items + item)
            snapshotBudget()
        }
    }

    override suspend fun updateItem(
        itemId: String,
        description: String,
        notes: String?,
    ): Result<BudgetView> = runCatching {
        mutex.withLock {
            require(description.isNotBlank()) { "Beskrivning krävs" }
            mutateItem(itemId) { existing ->
                existing.copy(
                    description = description.trim(),
                    notes = notes?.trim()?.ifBlank { null },
                )
            }
            snapshotBudget()
        }
    }

    override suspend fun markItemPaid(
        itemId: String,
        amount: Long,
        paidAt: String,
    ): Result<BudgetView> = runCatching {
        mutex.withLock {
            require(amount >= 0) { "Belopp kan inte vara negativt" }
            require(paidAt.isNotBlank()) { "Betaldatum krävs" }
            mutateItem(itemId) { it.copy(actualAmount = amount, paidAt = paidAt.trim()) }
            snapshotBudget()
        }
    }

    override suspend fun markItemUnpaid(itemId: String): Result<BudgetView> = runCatching {
        mutex.withLock {
            mutateItem(itemId) { it.copy(actualAmount = null, paidAt = null) }
            snapshotBudget()
        }
    }

    override suspend fun deleteItem(itemId: String): Result<BudgetView> = runCatching {
        mutex.withLock {
            var removed = false
            categories.forEachIndexed { index, category ->
                if (category.items.any { it.id == itemId }) {
                    categories[index] = category.copy(items = category.items.filterNot { it.id == itemId })
                    removed = true
                }
            }
            require(removed) { "Post $itemId finns inte" }
            snapshotBudget()
        }
    }

    override suspend fun listTodos(): Result<List<TodoItem>> = runCatching {
        mutex.withLock { todos.toList() }
    }

    override suspend fun createTodo(input: TodoInput): Result<TodoItem> = runCatching {
        mutex.withLock {
            validateTodoInput(input)
            val item = TodoItem(
                id = nextId("td"),
                title = input.title.trim(),
                dueDate = input.dueDate?.trim()?.ifBlank { null },
                status = TodoStatus.Open,
                assignee = input.assignee,
                notes = input.notes?.trim()?.ifBlank { null },
                createdAt = clock.nowIso(),
            )
            todos += item
            item
        }
    }

    override suspend fun updateTodo(id: String, input: TodoInput): Result<TodoItem> = runCatching {
        mutex.withLock {
            validateTodoInput(input)
            val index = todos.indexOfFirst { it.id == id }
            require(index >= 0) { "Uppgift $id finns inte" }
            val updated = todos[index].copy(
                title = input.title.trim(),
                dueDate = input.dueDate?.trim()?.ifBlank { null },
                assignee = input.assignee,
                notes = input.notes?.trim()?.ifBlank { null },
            )
            todos[index] = updated
            updated
        }
    }

    override suspend fun setTodoStatus(id: String, status: TodoStatus): Result<TodoItem> = runCatching {
        mutex.withLock {
            val index = todos.indexOfFirst { it.id == id }
            require(index >= 0) { "Uppgift $id finns inte" }
            val updated = todos[index].copy(status = status)
            todos[index] = updated
            updated
        }
    }

    override suspend fun deleteTodo(id: String): Result<Unit> = runCatching {
        mutex.withLock {
            val removed = todos.removeAll { it.id == id }
            require(removed) { "Uppgift $id finns inte" }
        }
    }

    private fun validateTodoInput(input: TodoInput) {
        require(input.title.isNotBlank()) { "Titel krävs" }
        val date = input.dueDate?.trim()
        if (!date.isNullOrEmpty()) {
            require(DATE_REGEX.matches(date)) { "Datum ska vara yyyy-MM-dd" }
        }
    }

    private fun mutateItem(itemId: String, transform: (BudgetItem) -> BudgetItem) {
        categories.forEachIndexed { index, category ->
            val itemIndex = category.items.indexOfFirst { it.id == itemId }
            if (itemIndex >= 0) {
                val newItems = category.items.toMutableList()
                newItems[itemIndex] = transform(newItems[itemIndex])
                categories[index] = category.copy(items = newItems)
                return
            }
        }
        error("Post $itemId finns inte")
    }

    private fun snapshotBudget(): BudgetView = BudgetView(
        totalCap = totalBudget,
        categories = categories.toList(),
    )

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
            Guest(nextId("g"), anderssonId, "Elin Andersson", false, RsvpStatus.Declined, null, "8 år"),
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

    private fun seedTodos() {
        val now = clock.nowIso()
        todos += TodoItem(
            id = nextId("td"),
            title = "Boka catering",
            dueDate = "2026-04-30",
            status = TodoStatus.Open,
            assignee = TodoAssignee.Me,
            notes = "Sara & Sons har skickat offert",
            createdAt = now,
        )
        todos += TodoItem(
            id = nextId("td"),
            title = "Skicka spara-datum-kort",
            dueDate = "2026-06-15",
            status = TodoStatus.Open,
            assignee = TodoAssignee.Both,
            notes = null,
            createdAt = now,
        )
        todos += TodoItem(
            id = nextId("td"),
            title = "Provsmakning tårta",
            dueDate = "2026-09-01",
            status = TodoStatus.Open,
            assignee = TodoAssignee.Partner,
            notes = null,
            createdAt = now,
        )
        todos += TodoItem(
            id = nextId("td"),
            title = "Bestäm gästlistans omfattning",
            dueDate = null,
            status = TodoStatus.Open,
            assignee = TodoAssignee.Both,
            notes = "Diskussion pågår",
            createdAt = now,
        )
        todos += TodoItem(
            id = nextId("td"),
            title = "Boka lokal",
            dueDate = "2026-03-01",
            status = TodoStatus.Done,
            assignee = TodoAssignee.Both,
            notes = "Klart — herrgården bokad",
            createdAt = now,
        )
    }

    private fun seedBudget() {
        totalBudget = 350_000L
        wedding = wedding.copy(totalBudget = 350_000L)

        val matId = nextId("c")
        categories += BudgetCategory(
            id = matId,
            name = "Mat",
            budgetedAmount = 80_000L,
            notes = null,
            items = listOf(
                BudgetItem(
                    id = nextId("bi"),
                    categoryId = matId,
                    description = "Catering — Sara & Sons",
                    actualAmount = 62_500L,
                    paidAt = "2027-01-15",
                    notes = "50 % betalt i förskott",
                ),
                BudgetItem(
                    id = nextId("bi"),
                    categoryId = matId,
                    description = "Bröllopstårta",
                    actualAmount = null,
                    paidAt = null,
                    notes = null,
                ),
            ),
        )

        val lokalId = nextId("c")
        categories += BudgetCategory(
            id = lokalId,
            name = "Lokal",
            budgetedAmount = 90_000L,
            notes = "Inkluderar dukar, stolar och städ",
            items = listOf(
                BudgetItem(
                    id = nextId("bi"),
                    categoryId = lokalId,
                    description = "Hyra herrgård",
                    actualAmount = null,
                    paidAt = null,
                    notes = "Slutbetalning på bröllopsdagen",
                ),
            ),
        )
    }

    companion object {
        private val DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")
    }
}
