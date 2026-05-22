package app.weddingplanner.ui.todo

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
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.Add
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
import app.weddingplanner.domain.TodoItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    viewModel: TodoListViewModel,
    onNewTodo: () -> Unit,
    onEditTodo: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Att-göra") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewTodo,
                text = { Text("Ny uppgift") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else -> TodoListContent(
                    state = state,
                    onSetStatusFilter = viewModel::setStatusFilter,
                    onSetAssigneeFilter = viewModel::setAssigneeFilter,
                    onToggle = viewModel::toggleStatus,
                    onEditTodo = onEditTodo,
                    onDelete = viewModel::delete,
                )
            }
        }
    }
}

@Composable
private fun TodoListContent(
    state: TodoListUiState,
    onSetStatusFilter: (TodoStatusFilter) -> Unit,
    onSetAssigneeFilter: (TodoAssigneeFilter) -> Unit,
    onToggle: (TodoItem) -> Unit,
    onEditTodo: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    val items = state.filtered
    Column(modifier = Modifier.fillMaxSize()) {
        FilterBar(
            statusFilter = state.statusFilter,
            assigneeFilter = state.assigneeFilter,
            myName = state.myName,
            partnerName = state.partnerName,
            onSetStatusFilter = onSetStatusFilter,
            onSetAssigneeFilter = onSetAssigneeFilter,
        )
        if (state.errorMessage != null) {
            Text(
                state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        HorizontalDivider()
        if (items.isEmpty()) {
            EmptyState(state)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 104.dp, top = 4.dp),
            ) {
                items(items, key = { it.id }) { item ->
                    TodoRow(
                        item = item,
                        today = state.today,
                        myName = state.myName,
                        partnerName = state.partnerName,
                        onToggle = { onToggle(item) },
                        onEdit = { onEditTodo(item.id) },
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
    statusFilter: TodoStatusFilter,
    assigneeFilter: TodoAssigneeFilter,
    myName: String,
    partnerName: String,
    onSetStatusFilter: (TodoStatusFilter) -> Unit,
    onSetAssigneeFilter: (TodoAssigneeFilter) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = statusFilter == TodoStatusFilter.Open,
                onClick = { onSetStatusFilter(TodoStatusFilter.Open) },
                label = { Text("Öppna") },
            )
            FilterChip(
                selected = statusFilter == TodoStatusFilter.All,
                onClick = { onSetStatusFilter(TodoStatusFilter.All) },
                label = { Text("Alla") },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = assigneeFilter == TodoAssigneeFilter.All,
                onClick = { onSetAssigneeFilter(TodoAssigneeFilter.All) },
                label = { Text("Alla") },
            )
            FilterChip(
                selected = assigneeFilter == TodoAssigneeFilter.Mine,
                onClick = { onSetAssigneeFilter(TodoAssigneeFilter.Mine) },
                label = { Text(myName) },
            )
            FilterChip(
                selected = assigneeFilter == TodoAssigneeFilter.Partners,
                onClick = { onSetAssigneeFilter(TodoAssigneeFilter.Partners) },
                label = { Text(partnerName) },
            )
        }
    }
}

@Composable
private fun EmptyState(state: TodoListUiState) {
    val isUnfiltered = state.statusFilter == TodoStatusFilter.All &&
        state.assigneeFilter == TodoAssigneeFilter.All
    val msg = if (state.items.isEmpty() || isUnfiltered) "Inget på listan ännu"
    else "Inga uppgifter matchar filtret"
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            Icons.AutoMirrored.Filled.PlaylistAddCheck,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(msg, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
