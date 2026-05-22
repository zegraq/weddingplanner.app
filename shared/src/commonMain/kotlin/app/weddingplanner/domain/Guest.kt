package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class Guest(
    val id: String,
    val householdId: String,
    val name: String,
    val isMainContact: Boolean,
    val rsvpStatus: RsvpStatus,
    val diet: String?,
    val notes: String?,
)

@Serializable
data class GuestInput(
    val name: String,
    val isMainContact: Boolean,
    val diet: String?,
    val notes: String?,
)
