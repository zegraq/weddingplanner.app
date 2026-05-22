package app.weddingplanner.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.TodoAssignee
import app.weddingplanner.domain.TodoInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val DATE_REGEX = Regex("""^\d{4}-\d{2}-\d{2}$""")

data class TodoEditUiState(
    val isLoading: Boolean = false,
    val title: String = "",
    val dueDate: String = "",
    val assignee: TodoAssignee = TodoAssignee.Both,
    val notes: String = "",
    val myName: String = "Daniel",
    val partnerName: String = "Sara",
    val saving: Boolean = false,
    val saved: Boolean = false,
    val deleted: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean
        get() = title.isNotBlank() && (dueDate.isBlank() || DATE_REGEX.matches(dueDate.trim()))
}

class TodoEditViewModel(
    private val apiClient: ApiClient,
    private val todoId: String?,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoEditUiState())
    val uiState: StateFlow<TodoEditUiState> = _uiState.asStateFlow()

    init {
        loadNames()
        if (todoId != null) loadExisting(todoId)
    }

    private fun loadNames() {
        viewModelScope.launch {
            runCatching { apiClient.getWedding() }
                .onSuccess { w ->
                    _uiState.value = _uiState.value.copy(myName = w.myName, partnerName = w.partnerName)
                }
        }
    }

    private fun loadExisting(id: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            apiClient.listTodos()
                .onSuccess { items ->
                    val item = items.firstOrNull { it.id == id }
                    if (item == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Uppgiften finns inte",
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            title = item.title,
                            dueDate = item.dueDate.orEmpty(),
                            assignee = item.assignee,
                            notes = item.notes.orEmpty(),
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta uppgiften",
                    )
                }
        }
    }

    fun update(block: TodoEditUiState.() -> TodoEditUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun save() {
        val current = _uiState.value
        if (!current.canSave) return
        val input = TodoInput(
            title = current.title.trim(),
            dueDate = current.dueDate.trim().ifBlank { null },
            assignee = current.assignee,
            notes = current.notes.trim().ifBlank { null },
        )
        viewModelScope.launch {
            _uiState.value = current.copy(saving = true, errorMessage = null)
            val result = if (todoId == null) apiClient.createTodo(input)
            else apiClient.updateTodo(todoId, input)
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
        val id = todoId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true, errorMessage = null)
            apiClient.deleteTodo(id)
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
