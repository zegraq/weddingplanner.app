package app.weddingplanner.ui.guests

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.RsvpStatus
import app.weddingplanner.ui.components.RsvpStatusDot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestListScreen(
    viewModel: GuestListViewModel,
    onHouseholdClick: (String) -> Unit,
    onNewHousehold: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("Gäster") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewHousehold,
                text = { Text("Nytt hushåll") },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            FilterRow(state.filter, viewModel::setFilter)
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage!!, viewModel::refresh)
                else -> {
                    val filtered = state.households.applyFilter(state.filter)
                    if (filtered.isEmpty()) {
                        EmptyState(state.filter)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(filtered, key = { it.id }) { household ->
                                HouseholdRow(household, onClick = { onHouseholdClick(household.id) })
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(selected: HouseholdFilter, onSelect: (HouseholdFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HouseholdFilter.values().forEach { filter ->
            FilterChip(
                selected = selected == filter,
                onClick = { onSelect(filter) },
                label = { Text(filter.label()) },
            )
        }
    }
}

private fun HouseholdFilter.label(): String = when (this) {
    HouseholdFilter.All -> "Alla"
    HouseholdFilter.Pending -> "Väntar svar"
    HouseholdFilter.Responded -> "Har svarat"
}

@Composable
private fun HouseholdRow(household: Household, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(household.displayName, style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                household.members.forEach { member ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RsvpStatusDot(member.rsvpStatus)
                        Text(
                            member.name,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (household.tags.isNotEmpty()) {
                Text(
                    household.tags.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            val attendingCount = household.members.count { it.rsvpStatus == RsvpStatus.Attending }
            val totalCount = household.members.size
            Text(
                "$attendingCount av $totalCount kommer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message)
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text("Försök igen")
            }
        }
    }
}

@Composable
private fun EmptyState(filter: HouseholdFilter) {
    val message = when (filter) {
        HouseholdFilter.All -> "Inga hushåll än. Tryck + för att lägga till."
        HouseholdFilter.Pending -> "Alla har svarat."
        HouseholdFilter.Responded -> "Ingen har svarat ännu."
    }
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
