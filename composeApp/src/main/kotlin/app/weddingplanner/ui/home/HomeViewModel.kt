package app.weddingplanner.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.api.Wedding
import app.weddingplanner.domain.BudgetView
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.RsvpStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HomeUiState(
    val isLoading: Boolean = true,
    val wedding: Wedding? = null,
    val households: List<Household> = emptyList(),
    val budget: BudgetView? = null,
    val daysLeft: Long? = null,
    val errorMessage: String? = null,
) {
    val guestCount: Int get() = households.sumOf { it.members.size }
    val attendingCount: Int get() = households.sumOf { household ->
        household.members.count { it.rsvpStatus == RsvpStatus.Attending }
    }
    val pendingHouseholds: Int get() = households.count { household ->
        household.members.any { it.rsvpStatus == RsvpStatus.Pending }
    }
}

class HomeViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val wedding = apiClient.getWedding()
            val households = apiClient.listHouseholds()
            val budget = apiClient.getBudget()

            val error = households.exceptionOrNull()?.message
                ?: budget.exceptionOrNull()?.message

            _uiState.value = HomeUiState(
                isLoading = false,
                wedding = wedding,
                households = households.getOrDefault(emptyList()),
                budget = budget.getOrNull(),
                daysLeft = daysUntil(wedding.date),
                errorMessage = error,
            )
        }
    }

    private fun daysUntil(date: String): Long? = runCatching {
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date))
    }.getOrNull()
}
