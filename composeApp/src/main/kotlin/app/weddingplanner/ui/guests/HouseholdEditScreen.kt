package app.weddingplanner.ui.guests

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdEditScreen(
    viewModel: HouseholdEditViewModel,
    isNew: Boolean,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isNew) "Nytt hushåll" else "Redigera hushåll") },
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
                EditForm(state, viewModel)
            }
        }
    }
}

@Composable
private fun EditForm(state: HouseholdEditUiState, vm: HouseholdEditViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = state.displayName,
            onValueChange = { v -> vm.update { copy(displayName = v) } },
            label = { Text("Visningsnamn (t.ex. Familjen Andersson)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.email,
            onValueChange = { v -> vm.update { copy(email = v) } },
            label = { Text("Email (valfritt)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.phone,
            onValueChange = { v -> vm.update { copy(phone = v) } },
            label = { Text("Telefon (valfritt)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.tagsText,
            onValueChange = { v -> vm.update { copy(tagsText = v) } },
            label = { Text("Taggar (kommaseparerade)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.notes,
            onValueChange = { v -> vm.update { copy(notes = v) } },
            label = { Text("Anteckningar (valfritt)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Medlemmar", style = MaterialTheme.typography.titleMedium)
        state.members.forEach { member ->
            MemberCard(member, vm, canRemove = state.members.size > 1)
        }
        OutlinedButton(onClick = { vm.addMember() }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(" Lägg till medlem", modifier = Modifier.padding(start = 4.dp))
        }

        if (state.errorMessage != null) {
            Text(
                state.errorMessage!!,
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
    }
}

@Composable
private fun MemberCard(
    member: MemberDraft,
    vm: HouseholdEditViewModel,
    canRemove: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = member.isMainContact,
                        onClick = { vm.setMainContact(member.key) },
                    )
                    Text("Huvudansvarig")
                }
                if (canRemove) {
                    IconButton(onClick = { vm.removeMember(member.key) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Ta bort medlem")
                    }
                }
            }
            OutlinedTextField(
                value = member.name,
                onValueChange = { v -> vm.updateMember(member.key) { copy(name = v) } },
                label = { Text("Namn") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = member.diet,
                onValueChange = { v -> vm.updateMember(member.key) { copy(diet = v) } },
                label = { Text("Kostpreferens / allergier (valfritt)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            OutlinedTextField(
                value = member.notes,
                onValueChange = { v -> vm.updateMember(member.key) { copy(notes = v) } },
                label = { Text("Anteckningar (valfritt)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
    }
}
