package app.weddingplanner.api

import app.weddingplanner.domain.BudgetView
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.HouseholdInput
import app.weddingplanner.domain.RsvpStatus
import app.weddingplanner.domain.ShoppingItem
import app.weddingplanner.domain.ShoppingItemInput
import app.weddingplanner.domain.TodoInput
import app.weddingplanner.domain.TodoItem
import app.weddingplanner.domain.TodoStatus

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

    suspend fun listTodos(): Result<List<TodoItem>>
    suspend fun createTodo(input: TodoInput): Result<TodoItem>
    suspend fun updateTodo(id: String, input: TodoInput): Result<TodoItem>
    suspend fun setTodoStatus(id: String, status: TodoStatus): Result<TodoItem>
    suspend fun deleteTodo(id: String): Result<Unit>

    suspend fun listShopping(): Result<List<ShoppingItem>>
    suspend fun createShopping(input: ShoppingItemInput): Result<ShoppingItem>
    suspend fun updateShopping(id: String, input: ShoppingItemInput): Result<ShoppingItem>
    suspend fun setShoppingBought(id: String, boughtAt: String?): Result<ShoppingItem>
    suspend fun deleteShopping(id: String): Result<Unit>
}
