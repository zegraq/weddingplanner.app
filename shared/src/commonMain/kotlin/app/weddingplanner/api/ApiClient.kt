package app.weddingplanner.api

import app.weddingplanner.domain.Household
import app.weddingplanner.domain.HouseholdInput
import app.weddingplanner.domain.RsvpStatus

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
}
