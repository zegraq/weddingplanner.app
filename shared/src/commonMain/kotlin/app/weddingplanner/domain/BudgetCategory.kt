package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class BudgetCategory(
    val id: String,
    val name: String,
    val budgetedAmount: Long,
    val notes: String?,
    val items: List<BudgetItem>,
) {
    val paidTotal: Long get() = items.sumOf { it.actualAmount ?: 0L }
    val remaining: Long get() = budgetedAmount - paidTotal
    val pendingCount: Int get() = items.count { !it.isPaid }
}
