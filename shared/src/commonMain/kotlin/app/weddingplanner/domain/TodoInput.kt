package app.weddingplanner.domain

import kotlinx.serialization.Serializable

@Serializable
data class TodoInput(
    val title: String,
    val dueDate: String?,
    val assignee: TodoAssignee,
    val notes: String?,
)
