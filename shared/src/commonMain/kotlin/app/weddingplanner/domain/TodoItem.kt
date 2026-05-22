package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class TodoItem(
    val id: String,
    val title: String,
    val dueDate: String?,
    val status: TodoStatus,
    val assignee: TodoAssignee,
    val notes: String?,
    val createdAt: String,
) {
    val isOpen: Boolean get() = status == TodoStatus.Open
    val isDone: Boolean get() = status == TodoStatus.Done

    fun isOverdueOn(today: String): Boolean =
        isOpen && dueDate != null && dueDate < today

    fun isDueOn(today: String): Boolean =
        isOpen && dueDate != null && dueDate == today
}
