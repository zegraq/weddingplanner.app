package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
enum class RsvpStatus {
    Pending,
    Attending,
    Declined,
}
