package app.weddingplanner.ui.guests

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.Guest
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.RsvpStatus
import app.weddingplanner.ui.components.RsvpStatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdDetailScreen(
    viewModel: HouseholdDetailViewModel,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(state.deleted) {
        if (state.deleted) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.household?.displayName ?: "Hushåll") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Tillbaka")
                    }
                },
                actions = {
                    state.household?.let { hh ->
                        IconButton(onClick = { onEdit(hh.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Redigera")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Ta bort")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
                state.household != null -> HouseholdDetailContent(
                    household = state.household!!,
                    onRsvpChange = viewModel::setRsvp,
                    onCopyLink = { link ->
                        copyToClipboard(context, "OSA-länk", link)
                        Toast.makeText(context, "Länk kopierad", Toast.LENGTH_SHORT).show()
                    },
                )
                state.errorMessage != null -> Text(
                    state.errorMessage!!,
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Ta bort hushåll?") },
            text = { Text("Detta kan inte ångras.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.delete()
                }) { Text("Ta bort") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Avbryt") }
            },
        )
    }
}

@Composable
private fun HouseholdDetailContent(
    household: Household,
    onRsvpChange: (String, RsvpStatus) -> Unit,
    onCopyLink: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ContactCard(household)
        TagsRow(household)
        MembersCard(household, onRsvpChange)
        RsvpLinkCard(household, onCopyLink)
        if (!household.notes.isNullOrBlank()) {
            NotesCard(household.notes!!)
        }
    }
}

@Composable
private fun ContactCard(household: Household) {
    if (household.email == null && household.phone == null) return
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Kontakt", style = MaterialTheme.typography.titleSmall)
            household.email?.let { Text("Email: $it") }
            household.phone?.let { Text("Telefon: $it") }
        }
    }
}

@Composable
private fun TagsRow(household: Household) {
    if (household.tags.isEmpty()) return
    Text(
        household.tags.joinToString(" · "),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun MembersCard(
    household: Household,
    onRsvpChange: (String, RsvpStatus) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Medlemmar (${household.members.size})", style = MaterialTheme.typography.titleSmall)
            household.members.forEachIndexed { index, member ->
                if (index > 0) HorizontalDivider()
                MemberRow(member, onRsvpChange)
            }
        }
    }
}

@Composable
private fun MemberRow(member: Guest, onRsvpChange: (String, RsvpStatus) -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.padding(end = 8.dp)) {
            Text(member.name, style = MaterialTheme.typography.bodyLarge)
            val sub = buildList {
                if (member.isMainContact) add("Huvudansvarig")
                member.diet?.takeIf { it.isNotBlank() }?.let { add(it) }
                member.notes?.takeIf { it.isNotBlank() }?.let { add(it) }
            }.joinToString(" · ")
            if (sub.isNotBlank()) {
                Text(
                    sub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Box {
            TextButton(onClick = { menuOpen = true }) {
                RsvpStatusChip(member.rsvpStatus)
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                RsvpStatus.entries.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status.swedishLabel()) },
                        onClick = {
                            menuOpen = false
                            onRsvpChange(member.id, status)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun RsvpLinkCard(household: Household, onCopy: (String) -> Unit) {
    val link = household.rsvpLink()
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("OSA-länk", style = MaterialTheme.typography.titleSmall)
            Text(
                link,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { onCopy(link) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Text(" Kopiera länk", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Anteckningar", style = MaterialTheme.typography.titleSmall)
            Text(notes)
        }
    }
}

private fun RsvpStatus.swedishLabel(): String = when (this) {
    RsvpStatus.Pending -> "Väntar svar"
    RsvpStatus.Attending -> "Kommer"
    RsvpStatus.Declined -> "Kommer inte"
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
