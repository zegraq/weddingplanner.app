package app.weddingplanner.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.BudgetView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BudgetUiState(
    val isLoading: Boolean = true,
    val view: BudgetView? = null,
    val errorMessage: String? = null,
)

class BudgetViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            apiClient.getBudget()
                .onSuccess { _uiState.value = BudgetUiState(isLoading = false, view = it) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta budget",
                    )
                }
        }
    }

    fun setTotal(amount: Long?) = mutate { apiClient.setTotalBudget(amount) }

    fun saveCategory(id: String?, name: String, budget: Long, notes: String?) = mutate {
        if (id == null) apiClient.createCategory(name, budget, notes)
        else apiClient.updateCategory(id, name, budget, notes)
    }

    fun deleteCategory(id: String) = mutate { apiClient.deleteCategory(id) }

    fun saveItem(
        categoryId: String,
        itemId: String?,
        description: String,
        notes: String?,
    ) = mutate {
        if (itemId == null) apiClient.addItem(categoryId, description, notes)
        else apiClient.updateItem(itemId, description, notes)
    }

    fun deleteItem(itemId: String) = mutate { apiClient.deleteItem(itemId) }

    fun payItem(itemId: String, amount: Long, paidAt: String) = mutate {
        apiClient.markItemPaid(itemId, amount, paidAt)
    }

    fun unpayItem(itemId: String) = mutate { apiClient.markItemUnpaid(itemId) }

    private fun mutate(block: suspend () -> Result<BudgetView>) {
        viewModelScope.launch {
            block()
                .onSuccess { _uiState.value = _uiState.value.copy(view = it, errorMessage = null) }
                .onFailure { _uiState.value = _uiState.value.copy(errorMessage = it.message) }
        }
    }
}
