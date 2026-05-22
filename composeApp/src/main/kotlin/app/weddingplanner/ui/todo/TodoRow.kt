package app.weddingplanner.ui.todo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.TodoAssignee
import app.weddingplanner.domain.TodoItem

private val OverdueColor = Color(0xFFC62828)
private val TodayColor = Color(0xFFEF6C00)

@Composable
fun TodoRow(
    item: TodoItem,
    today: String,
    myName: String,
    partnerName: String,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = item.isDone, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
            Text(
                item.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isDone) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val due = item.dueDate
            val isOverdue = item.isOverdueOn(today)
            val isToday = item.isDueOn(today)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (due != null) {
                    Text(
                        due,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            isOverdue -> OverdueColor
                            isToday -> TodayColor
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                    if (isOverdue) {
                        Text(
                            "  · Försenad",
                            style = MaterialTheme.typography.labelMedium,
                            color = OverdueColor,
                        )
                    } else if (isToday) {
                        Text(
                            "  · Idag",
                            style = MaterialTheme.typography.labelMedium,
                            color = TodayColor,
                        )
                    }
                } else {
                    Text(
                        "Ingen deadline",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!item.notes.isNullOrBlank()) {
                    Text(
                        "  · ${item.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        AssignChip(item.assignee, myName, partnerName)
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Mer", modifier = Modifier.size(20.dp))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("Ta bort") },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    onClick = { menuOpen = false; onDelete() },
                )
            }
        }
    }
}

@Composable
private fun AssignChip(assignee: TodoAssignee, myName: String, partnerName: String) {
    val label = when (assignee) {
        TodoAssignee.Me -> myName
        TodoAssignee.Partner -> partnerName
        TodoAssignee.Both -> "Båda"
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label, maxLines = 1) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = Modifier.padding(horizontal = 4.dp),
    )
}

@Composable
fun TodoAssigneeChip(
    assignee: TodoAssignee,
    myName: String,
    partnerName: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val label = when (assignee) {
        TodoAssignee.Me -> myName
        TodoAssignee.Partner -> partnerName
        TodoAssignee.Both -> "Båda"
    }
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.padding(end = 8.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface,
            labelColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
        ),
    )
}

