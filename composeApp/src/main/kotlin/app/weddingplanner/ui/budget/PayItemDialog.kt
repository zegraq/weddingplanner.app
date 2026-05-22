package app.weddingplanner.ui.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.BudgetItem

@Composable
fun PayItemDialog(
    item: BudgetItem,
    onDismiss: () -> Unit,
    onConfirm: (amount: Long, paidAt: String) -> Unit,
) {
    var amount by remember { mutableStateOf(item.actualAmount?.toString().orEmpty()) }
    var paidAt by remember { mutableStateOf(item.paidAt ?: todayIso()) }
    val parsedAmount = parseSek(amount)
    val canSave = parsedAmount != null && parsedAmount >= 0 && paidAt.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Markera som betald") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.description)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Faktiskt belopp i kr") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = paidAt,
                    onValueChange = { paidAt = it },
                    label = { Text("Betaldatum (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = canSave,
                onClick = { onConfirm(parsedAmount!!, paidAt.trim()) },
            ) { Text("Spara") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Avbryt") }
        },
    )
}
