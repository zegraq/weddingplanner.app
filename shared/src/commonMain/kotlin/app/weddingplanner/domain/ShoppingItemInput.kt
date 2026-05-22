package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItemInput(
    val name: String,
    val quantity: Int,
    val store: String?,
    val notes: String?,
)
