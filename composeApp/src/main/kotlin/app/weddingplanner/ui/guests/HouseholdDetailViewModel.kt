package app.weddingplanner.ui.guests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.RsvpStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HouseholdDetailUiState(
    val isLoading: Boolean = true,
    val household: Household? = null,
    val errorMessage: String? = null,
    val deleted: Boolean = false,
)

class HouseholdDetailViewModel(
    private val apiClient: ApiClient,
    private val householdId: String,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HouseholdDetailUiState())
    val uiState: StateFlow<HouseholdDetailUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            apiClient.getHousehold(householdId)
                .onSuccess { _uiState.value = HouseholdDetailUiState(isLoading = false, household = it) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta hushållet",
                    )
                }
        }
    }

    fun setRsvp(guestId: String, status: RsvpStatus) {
        viewModelScope.launch {
            apiClient.setGuestRsvpStatus(householdId, guestId, status)
                .onSuccess { _uiState.value = _uiState.value.copy(household = it) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message)
                }
        }
    }

    fun delete() {
        viewModelScope.launch {
            apiClient.deleteHousehold(householdId)
                .onSuccess { _uiState.value = _uiState.value.copy(deleted = true) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message)
                }
        }
    }
}

const val RSVP_LINK_BASE = "https://weddingplanner.local/rsvp"

fun Household.rsvpLink(): String = "$RSVP_LINK_BASE/$rsvpToken"
