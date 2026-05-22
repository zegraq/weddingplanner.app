package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class BudgetView(
    val totalCap: Long?,
    val categories: List<BudgetCategory>,
) {
    val plannedTotal: Long get() = categories.sumOf { it.budgetedAmount }
    val paidTotal: Long get() = categories.sumOf { it.paidTotal }

    val unallocated: Long?
        get() = totalCap?.let { it - plannedTotal }

    val isOverCap: Boolean
        get() = totalCap != null && plannedTotal > totalCap
}
