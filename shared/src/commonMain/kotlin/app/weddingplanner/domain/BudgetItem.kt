package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class BudgetItem(
    val id: String,
    val categoryId: String,
    val description: String,
    val actualAmount: Long?,
    val paidAt: String?,
    val notes: String?,
) {
    val isPaid: Boolean get() = actualAmount != null && paidAt != null
}
