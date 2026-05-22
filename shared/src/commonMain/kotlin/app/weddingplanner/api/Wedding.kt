package app.weddingplanner.api

import kotlinx.serialization.Serializable

@Serializable
data class Wedding(
    val date: String,
    val venue: String?,
    val totalBudget: Long? = null,
    val myName: String = "Daniel",
    val partnerName: String = "Sara",
)
