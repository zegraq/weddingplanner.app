package app.weddingplanner.api

import app.weddingplanner.domain.BudgetView
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.HouseholdInput
import app.weddingplanner.domain.RsvpStatus

interface ApiClient {
    suspend fun getWedding(): Wedding

    suspend fun listHouseholds(): Result<List<Household>>
    suspend fun getHousehold(id: String): Result<Household>
    suspend fun createHousehold(input: HouseholdInput): Result<Household>
    suspend fun updateHousehold(id: String, input: HouseholdInput): Result<Household>
    suspend fun deleteHousehold(id: String): Result<Unit>
    suspend fun setGuestRsvpStatus(
        householdId: String,
        guestId: String,
        status: RsvpStatus,
    ): Result<Household>

    suspend fun getBudget(): Result<BudgetView>
    suspend fun setTotalBudget(amount: Long?): Result<BudgetView>
    suspend fun createCategory(
        name: String,
        budgetedAmount: Long,
        notes: String?,
    ): Result<BudgetView>
    suspend fun updateCategory(
        id: String,
        name: String,
        budgetedAmount: Long,
        notes: String?,
    ): Result<BudgetView>
    suspend fun deleteCategory(id: String): Result<BudgetView>
    suspend fun addItem(
        categoryId: String,
        description: String,
        notes: String?,
    ): Result<BudgetView>
    suspend fun updateItem(
        itemId: String,
        description: String,
        notes: String?,
    ): Result<BudgetView>
    suspend fun markItemPaid(
        itemId: String,
        amount: Long,
        paidAt: String,
    ): Result<BudgetView>
    suspend fun markItemUnpaid(itemId: String): Result<BudgetView>
    suspend fun deleteItem(itemId: String): Result<BudgetView>
}
