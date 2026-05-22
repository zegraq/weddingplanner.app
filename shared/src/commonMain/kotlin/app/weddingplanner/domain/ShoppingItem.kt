package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val store: String?,
    val notes: String?,
    val boughtAt: String?,
    val createdAt: String,
) {
    val isBought: Boolean get() = boughtAt != null
}
