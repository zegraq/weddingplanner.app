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

enum class HouseholdFilter { All, Pending, Responded }

data class GuestListUiState(
    val isLoading: Boolean = true,
    val households: List<Household> = emptyList(),
    val filter: HouseholdFilter = HouseholdFilter.All,
    val errorMessage: String? = null,
)

class GuestListViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _uiState = MutableStateFlow(GuestListUiState())
    val uiState: StateFlow<GuestListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            apiClient.listHouseholds()
                .onSuccess { list ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        households = list.sortedBy { it.displayName },
                    )
                }
                .onFailure { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Kunde inte hämta hushåll",
                    )
                }
        }
    }

    fun setFilter(filter: HouseholdFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }
}

fun List<Household>.applyFilter(filter: HouseholdFilter): List<Household> = when (filter) {
    HouseholdFilter.All -> this
    HouseholdFilter.Pending -> filter { it.members.any { m -> m.rsvpStatus == RsvpStatus.Pending } }
    HouseholdFilter.Responded -> filter { it.members.all { m -> m.rsvpStatus != RsvpStatus.Pending } }
}
