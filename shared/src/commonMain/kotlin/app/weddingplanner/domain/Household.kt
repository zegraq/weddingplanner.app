package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class Household(
    val id: String,
    val displayName: String,
    val email: String?,
    val phone: String?,
    val tags: List<String>,
    val rsvpToken: String,
    val rsvpRespondedAt: String?,
    val notes: String?,
    val members: List<Guest>,
)

@Serializable
data class HouseholdInput(
    val displayName: String,
    val email: String?,
    val phone: String?,
    val tags: List<String>,
    val notes: String?,
    val members: List<GuestInput>,
)
