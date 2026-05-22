package app.weddingplanner.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.BudgetItem

@Composable
fun EditItemDialog(
    existing: BudgetItem?,
    onDismiss: () -> Unit,
    onSave: (itemId: String?, description: String, notes: String?) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    var description by remember { mutableStateOf(existing?.description.orEmpty()) }
    var notes by remember { mutableStateOf(existing?.notes.orEmpty()) }
    val canSave = description.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Ny post" else "Redigera post") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Beskrivning") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Anteckningar (valfritt)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onSave(existing?.id, description.trim(), notes.trim().ifBlank { null }) },
            ) { Text("Spara") }
        },
        dismissButton = {
            if (onDelete != null) {
                TextButton(onClick = onDelete) { Text("Ta bort") }
            } else {
                TextButton(onClick = onDismiss) { Text("Avbryt") }
            }
        },
    )
}
