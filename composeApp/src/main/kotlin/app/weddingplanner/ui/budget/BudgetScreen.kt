package app.weddingplanner.ui.budget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.BudgetCategory
import app.weddingplanner.domain.BudgetItem
import app.weddingplanner.domain.BudgetView

private val OverCapColor = Color(0xFFC62828)
private val PaidColor = Color(0xFF2E7D32)
private val PendingColor = Color(0xFFEF6C00)

private sealed interface DialogState {
    data object EditTotal : DialogState
    data object NewCategory : DialogState
    data class EditCategory(val category: BudgetCategory) : DialogState
    data class NewItem(val categoryId: String) : DialogState
    data class EditItem(val item: BudgetItem) : DialogState
    data class PayItem(val item: BudgetItem) : DialogState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val state by viewModel.uiState.collectAsState()
    var dialog by remember { mutableStateOf<DialogState?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Budget") }) },
        floatingActionButton = {
            if (state.view != null) {
                ExtendedFloatingActionButton(
                    onClick = { dialog = DialogState.NewCategory },
                    text = { Text("Ny kategori") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.view != null -> BudgetContent(
                    view = state.view!!,
                    errorMessage = state.errorMessage,
                    onEditTotal = { dialog = DialogState.EditTotal },
                    onAddCategory = { dialog = DialogState.NewCategory },
                    onEditCategory = { dialog = DialogState.EditCategory(it) },
                    onAddItem = { dialog = DialogState.NewItem(it) },
                    onEditItem = { dialog = DialogState.EditItem(it) },
                    onPayItem = { dialog = DialogState.PayItem(it) },
                    onUnpayItem = viewModel::unpayItem,
                )
                state.errorMessage != null -> Text(
                    state.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
        }
    }

    when (val d = dialog) {
        null -> Unit
        DialogState.EditTotal -> EditTotalDialog(
            currentTotal = state.view?.totalCap,
            onDismiss = { dialog = null },
            onSave = { newTotal -> viewModel.setTotal(newTotal); dialog = null },
        )
        DialogState.NewCategory -> EditCategoryDialog(
            existing = null,
            onDismiss = { dialog = null },
            onSave = { _, name, budget, notes ->
                viewModel.saveCategory(null, name, budget, notes); dialog = null
            },
        )
        is DialogState.EditCategory -> EditCategoryDialog(
            existing = d.category,
            onDismiss = { dialog = null },
            onSave = { id, name, budget, notes ->
                viewModel.saveCategory(id, name, budget, notes); dialog = null
            },
            onDelete = { viewModel.deleteCategory(d.category.id); dialog = null },
        )
        is DialogState.NewItem -> EditItemDialog(
            existing = null,
            onDismiss = { dialog = null },
            onSave = { _, desc, notes ->
                viewModel.saveItem(d.categoryId, null, desc, notes); dialog = null
            },
        )
        is DialogState.EditItem -> EditItemDialog(
            existing = d.item,
            onDismiss = { dialog = null },
            onSave = { id, desc, notes ->
                viewModel.saveItem(d.item.categoryId, id, desc, notes); dialog = null
            },
            onDelete = { viewModel.deleteItem(d.item.id); dialog = null },
        )
        is DialogState.PayItem -> PayItemDialog(
            item = d.item,
            onDismiss = { dialog = null },
            onConfirm = { amount, paidAt ->
                viewModel.payItem(d.item.id, amount, paidAt); dialog = null
            },
        )
    }
}

@Composable
private fun BudgetContent(
    view: BudgetView,
    errorMessage: String?,
    onEditTotal: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (BudgetCategory) -> Unit,
    onAddItem: (String) -> Unit,
    onEditItem: (BudgetItem) -> Unit,
    onPayItem: (BudgetItem) -> Unit,
    onUnpayItem: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { TotalHeaderCard(view, onEditTotal) }
        if (errorMessage != null) {
            item {
                Text(
                    errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        if (view.categories.isEmpty()) {
            item { EmptyState(onAddCategory) }
        } else {
            items(view.categories, key = { it.id }) { category ->
                CategoryCard(
                    category = category,
                    onEditCategory = { onEditCategory(category) },
                    onAddItem = { onAddItem(category.id) },
                    onEditItem = onEditItem,
                    onPayItem = onPayItem,
                    onUnpayItem = onUnpayItem,
                )
            }
        }
    }
}

@Composable
private fun TotalHeaderCard(view: BudgetView, onEditTotal: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEditTotal),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Totalbudget", style = MaterialTheme.typography.titleSmall)
            Text(
                formatSek(view.totalCap),
                style = MaterialTheme.typography.headlineSmall,
                color = if (view.isOverCap) OverCapColor else MaterialTheme.colorScheme.onSurface,
            )
            HorizontalDivider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Planerat (kategorier)", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatSek(view.plannedTotal))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Hittills betalt", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatSek(view.paidTotal))
            }
            if (view.totalCap != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val remaining = view.unallocated ?: 0L
                    Text(
                        if (remaining >= 0) "Kvar att fördela" else "Över taket",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        formatSek(remaining),
                        color = if (remaining < 0) OverCapColor else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: BudgetCategory,
    onEditCategory: () -> Unit,
    onAddItem: () -> Unit,
    onEditItem: (BudgetItem) -> Unit,
    onPayItem: (BudgetItem) -> Unit,
    onUnpayItem: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onEditCategory),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(category.name, style = MaterialTheme.typography.titleMedium)
                    if (!category.notes.isNullOrBlank()) {
                        Text(
                            category.notes!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(formatSek(category.budgetedAmount), style = MaterialTheme.typography.titleSmall)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Betalt ${formatSek(category.paidTotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Kvar ${formatSek(category.remaining)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (category.remaining < 0) OverCapColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (category.items.isNotEmpty()) HorizontalDivider()
            category.items.forEach { item ->
                ItemRow(
                    item = item,
                    onEdit = { onEditItem(item) },
                    onPay = { onPayItem(item) },
                    onUnpay = { onUnpayItem(item.id) },
                )
            }
            OutlinedButton(onClick = onAddItem) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(" Lägg till post", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun ItemRow(
    item: BudgetItem,
    onEdit: () -> Unit,
    onPay: () -> Unit,
    onUnpay: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                item.description,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val sub = buildList {
                if (item.isPaid) {
                    add(formatSek(item.actualAmount))
                    item.paidAt?.let { add(it) }
                } else {
                    add("Väntar betalning")
                }
                item.notes?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" · ")
            Text(
                sub,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (item.isPaid) {
            AssistChip(
                modifier = Modifier.widthIn(min = 112.dp),
                onClick = onUnpay,
                label = { Text("Betald", maxLines = 1) },
                leadingIcon = { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp)) },
                colors = AssistChipDefaults.assistChipColors(labelColor = PaidColor, leadingIconContentColor = PaidColor),
            )
        } else {
            TextButton(
                modifier = Modifier.widthIn(min = 112.dp),
                onClick = onPay,
            ) {
                Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                Text(" Betald?", color = PendingColor, maxLines = 1)
            }
        }
    }
}

@Composable
private fun EmptyState(onAddCategory: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Default.HourglassEmpty,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Inga kategorier än. Börja med att lägga till t.ex. \"Mat\" eller \"Lokal\".",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedButton(onClick = onAddCategory) { Text("Lägg till första kategorin") }
    }
}
