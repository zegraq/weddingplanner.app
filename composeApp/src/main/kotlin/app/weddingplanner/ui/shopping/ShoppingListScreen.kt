package app.weddingplanner.ui.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingListViewModel,
    onNewItem: () -> Unit,
    onEditItem: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Inköp") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewItem,
                text = { Text("Ny post") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else -> ShoppingContent(
                    state = state,
                    onSetFilter = viewModel::setFilter,
                    onToggle = viewModel::toggleBought,
                    onEdit = onEditItem,
                    onDelete = viewModel::delete,
                )
            }
        }
    }
}

@Composable
private fun ShoppingContent(
    state: ShoppingListUiState,
    onSetFilter: (ShoppingFilter) -> Unit,
    onToggle: (ShoppingItem) -> Unit,
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FilterBar(
            filter = state.filter,
            pendingCount = state.pendingCount,
            totalCount = state.items.size,
            onSetFilter = onSetFilter,
        )
        if (state.errorMessage != null) {
            Text(
                state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        HorizontalDivider()
        val items = state.filtered
        if (items.isEmpty()) {
            EmptyState(state)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 104.dp, top = 4.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    ShoppingRow(
                        item = item,
                        onToggle = { onToggle(item) },
                        onEdit = { onEdit(item.id) },
                        onDelete = { onDelete(item.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    filter: ShoppingFilter,
    pendingCount: Int,
    totalCount: Int,
    onSetFilter: (ShoppingFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilterChip(
            selected = filter == ShoppingFilter.Pending,
            onClick = { onSetFilter(ShoppingFilter.Pending) },
            label = { Text("Att köpa ($pendingCount)") },
        )
        FilterChip(
            selected = filter == ShoppingFilter.All,
            onClick = { onSetFilter(ShoppingFilter.All) },
            label = { Text("Alla ($totalCount)") },
        )
    }
}

@Composable
private fun EmptyState(state: ShoppingListUiState) {
    val msg = if (state.items.isEmpty()) "Inget på listan ännu"
    else "Allt är köpt — bra jobbat!"
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(msg, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
