package app.weddingplanner.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.ShoppingItemInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ShoppingEditUiState(
    val isLoading: Boolean = false,
    val name: String = "",
    val quantityText: String = "1",
    val store: String = "",
    val notes: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val errorMessage: String? = null,
) {
    val quantity: Int? get() = quantityText.trim().toIntOrNull()
    val canSave: Boolean
        get() = name.isNotBlank() && (quantity?.let { it >= 1 } ?: false)
}

class ShoppingEditViewModel(
    private val apiClient: ApiClient,
    private val itemId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShoppingEditUiState())
    val uiState: StateFlow<ShoppingEditUiState> = _uiState.asStateFlow()

    init {
        if (itemId != null) loadExisting(itemId)
    }

    private fun loadExisting(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            apiClient.listShopping()
                .onSuccess { items ->
                    val item = items.firstOrNull { it.id == id }
                    if (item == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Inköpsposten finns inte",
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            name = item.name,
                            quantityText = item.quantity.toString(),
                            store = item.store.orEmpty(),
                            notes = item.notes.orEmpty(),
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta posten",
                    )
                }
        }
    }

    fun update(block: ShoppingEditUiState.() -> ShoppingEditUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun save() {
        val current = _uiState.value
        val qty = current.quantity ?: return
        if (!current.canSave) return
        val input = ShoppingItemInput(
            name = current.name.trim(),
            quantity = qty,
            store = current.store.trim().ifBlank { null },
            notes = current.notes.trim().ifBlank { null },
        )
        viewModelScope.launch {
            _uiState.value = current.copy(saving = true, errorMessage = null)
            val result = if (itemId == null) apiClient.createShopping(input)
            else apiClient.updateShopping(itemId, input)
            result
                .onSuccess { _uiState.value = _uiState.value.copy(saving = false, saved = true) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        saving = false,
                        errorMessage = it.message ?: "Kunde inte spara",
                    )
                }
        }
    }

    fun delete() {
        val id = itemId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, errorMessage = null)
            apiClient.deleteShopping(id)
                .onSuccess { _uiState.value = _uiState.value.copy(saving = false, deleted = true) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        saving = false,
                        errorMessage = it.message ?: "Kunde inte ta bort",
                    )
                }
        }
    }
}
