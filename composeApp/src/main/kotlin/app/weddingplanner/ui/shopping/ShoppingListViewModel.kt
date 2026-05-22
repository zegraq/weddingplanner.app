package app.weddingplanner.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

enum class ShoppingFilter { Pending, All }

data class ShoppingListUiState(
    val isLoading: Boolean = true,
    val items: List<ShoppingItem> = emptyList(),
    val filter: ShoppingFilter = ShoppingFilter.Pending,
    val errorMessage: String? = null,
) {
    val filtered: List<ShoppingItem>
        get() {
            val source = when (filter) {
                ShoppingFilter.Pending -> items.filterNot { it.isBought }
                ShoppingFilter.All -> items
            }
            return source.sortedWith(ShoppingComparator)
        }

    val pendingCount: Int get() = items.count { !it.isBought }
    val boughtCount: Int get() = items.count { it.isBought }
}

private val ShoppingComparator: Comparator<ShoppingItem> = Comparator { a, b ->
    val boughtCmp = a.isBought.compareTo(b.isBought)
    if (boughtCmp != 0) return@Comparator boughtCmp
    val nameCmp = a.name.compareTo(b.name, ignoreCase = true)
    if (nameCmp != 0) nameCmp else a.createdAt.compareTo(b.createdAt)
}

class ShoppingListViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingListUiState())
    val uiState: StateFlow<ShoppingListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            apiClient.listShopping()
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(isLoading = false, items = items)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta inköp",
                    )
                }
        }
    }

    fun setFilter(filter: ShoppingFilter) {
        _uiState.value = _uiState.value.copy(filter = filter)
    }

    fun toggleBought(item: ShoppingItem) {
        val next = if (item.isBought) null else today()
        viewModelScope.launch {
            apiClient.setShoppingBought(item.id, next)
                .onSuccess { updated ->
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.map { if (it.id == updated.id) updated else it },
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message)
                }
        }
    }

    fun delete(itemId: String) {
        viewModelScope.launch {
            apiClient.deleteShopping(itemId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        items = _uiState.value.items.filterNot { it.id == itemId },
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(errorMessage = it.message)
                }
        }
    }

    private fun today(): String {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/Stockholm"))
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }
}
