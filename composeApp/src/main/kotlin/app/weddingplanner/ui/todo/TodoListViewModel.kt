package app.weddingplanner.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.weddingplanner.api.ApiClient
import app.weddingplanner.domain.TodoAssignee
import app.weddingplanner.domain.TodoItem
import app.weddingplanner.domain.TodoStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TodoStatusFilter { Open, All }
enum class TodoAssigneeFilter { All, Mine, Partners }

data class TodoListUiState(
    val isLoading: Boolean = true,
    val items: List<TodoItem> = emptyList(),
    val today: String = "",
    val myName: String = "Daniel",
    val partnerName: String = "Sara",
    val statusFilter: TodoStatusFilter = TodoStatusFilter.Open,
    val assigneeFilter: TodoAssigneeFilter = TodoAssigneeFilter.All,
    val errorMessage: String? = null,
) {
    val filtered: List<TodoItem>
        get() {
            val byStatus = when (statusFilter) {
                TodoStatusFilter.Open -> items.filter { it.status == TodoStatus.Open }
                TodoStatusFilter.All -> items
            }
            val byAssignee = when (assigneeFilter) {
                TodoAssigneeFilter.All -> byStatus
                TodoAssigneeFilter.Mine -> byStatus.filter {
                    it.assignee == TodoAssignee.Me || it.assignee == TodoAssignee.Both
                }
                TodoAssigneeFilter.Partners -> byStatus.filter {
                    it.assignee == TodoAssignee.Partner || it.assignee == TodoAssignee.Both
                }
            }
            return byAssignee.sortedWith(TodoComparator)
        }
}

private val TodoComparator: Comparator<TodoItem> = Comparator { a, b ->
    val statusCmp = a.status.ordinal.compareTo(b.status.ordinal)
    if (statusCmp != 0) return@Comparator statusCmp
    val aDate = a.dueDate
    val bDate = b.dueDate
    when {
        aDate == null && bDate == null -> a.createdAt.compareTo(b.createdAt)
        aDate == null -> 1
        bDate == null -> -1
        else -> {
            val dateCmp = aDate.compareTo(bDate)
            if (dateCmp != 0) dateCmp else a.createdAt.compareTo(b.createdAt)
        }
    }
}

class TodoListViewModel(private val apiClient: ApiClient) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val wedding = runCatching { apiClient.getWedding() }.getOrNull()
            apiClient.listTodos()
                .onSuccess { items ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        items = items,
                        today = todayFromIso(wedding?.date),
                        myName = wedding?.myName ?: _uiState.value.myName,
                        partnerName = wedding?.partnerName ?: _uiState.value.partnerName,
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = it.message ?: "Kunde inte hämta uppgifter",
                    )
                }
        }
    }

    fun setStatusFilter(filter: TodoStatusFilter) {
        _uiState.value = _uiState.value.copy(statusFilter = filter)
    }

    fun setAssigneeFilter(filter: TodoAssigneeFilter) {
        _uiState.value = _uiState.value.copy(assigneeFilter = filter)
    }

    fun toggleStatus(item: TodoItem) {
        val next = if (item.status == TodoStatus.Open) TodoStatus.Done else TodoStatus.Open
        viewModelScope.launch {
            apiClient.setTodoStatus(item.id, next)
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
            apiClient.deleteTodo(itemId)
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
}

private fun todayFromIso(@Suppress("UNUSED_PARAMETER") weddingDate: String?): String {
    // Använder systemets vägg-klocka via java.time här i Android-target;
    // shared/Clock används bara för server-side mutations.
    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Europe/Stockholm"))
    val y = cal.get(java.util.Calendar.YEAR)
    val m = cal.get(java.util.Calendar.MONTH) + 1
    val d = cal.get(java.util.Calendar.DAY_OF_MONTH)
    return "%04d-%02d-%02d".format(y, m, d)
}
