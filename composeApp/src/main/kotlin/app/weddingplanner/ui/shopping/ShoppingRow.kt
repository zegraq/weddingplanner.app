package app.weddingplanner.ui.shopping

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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.ShoppingItem

@Composable
fun ShoppingRow(
    item: ShoppingItem,
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
        Checkbox(checked = item.isBought, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f).padding(horizontal = 4.dp)) {
            val title = if (item.quantity > 1) "${item.name} × ${item.quantity}" else item.name
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (item.isBought) MaterialTheme.colorScheme.onSurfaceVariant
                else MaterialTheme.colorScheme.onSurface,
                textDecoration = if (item.isBought) TextDecoration.LineThrough else null,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val sub = buildList {
                item.store?.takeIf { it.isNotBlank() }?.let { add(it) }
                if (item.isBought) item.boughtAt?.let { add("Köpt $it") }
                item.notes?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" · ")
            if (sub.isNotEmpty()) {
                Text(
                    sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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
