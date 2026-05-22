package app.weddingplanner.ui.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.TodoAssignee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoEditScreen(
    viewModel: TodoEditViewModel,
    isNew: Boolean,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved, state.deleted) {
        if (state.saved || state.deleted) onBack()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Ny uppgift" else "Redigera uppgift") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.save() },
                        enabled = state.canSave && !state.saving,
                    ) { Text("Spara") }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                EditForm(state, viewModel, isNew)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditForm(state: TodoEditUiState, vm: TodoEditViewModel, isNew: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.title,
            onValueChange = { v -> vm.update { copy(title = v) } },
            label = { Text("Titel") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.title.isBlank(),
        )
        OutlinedTextField(
            value = state.dueDate,
            onValueChange = { v -> vm.update { copy(dueDate = v) } },
            label = { Text("Deadline (yyyy-MM-dd, valfritt)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("2027-06-05") },
        )

        Text("Tilldelad", style = MaterialTheme.typography.titleSmall)
        val options = listOf(TodoAssignee.Me, TodoAssignee.Both, TodoAssignee.Partner)
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { i, option ->
                SegmentedButton(
                    selected = state.assignee == option,
                    onClick = { vm.update { copy(assignee = option) } },
                    shape = SegmentedButtonDefaults.itemShape(i, options.size),
                ) {
                    Text(
                        when (option) {
                            TodoAssignee.Me -> state.myName
                            TodoAssignee.Partner -> state.partnerName
                            TodoAssignee.Both -> "Båda"
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = state.notes,
            onValueChange = { v -> vm.update { copy(notes = v) } },
            label = { Text("Anteckningar (valfritt)") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.errorMessage != null) {
            Text(
                state.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Button(
            onClick = { vm.save() },
            enabled = state.canSave && !state.saving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.saving) "Sparar…" else "Spara")
        }

        if (!isNew) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(
                    onClick = { vm.delete() },
                    enabled = !state.saving,
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text(" Ta bort", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
