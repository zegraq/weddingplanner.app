package app.weddingplanner.ui.guests

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.weddingplanner.domain.Household
import app.weddingplanner.domain.RsvpStatus
import app.weddingplanner.ui.components.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestListScreen(
    viewModel: GuestListViewModel,
    onHouseholdClick: (String) -> Unit,
    onNewHousehold: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAllergies by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ScreenTopBar(
                onSearchClick = { searchActive = !searchActive },
                menuOpen = menuOpen,
                onMenuClick = { menuOpen = true },
                onDismissMenu = { menuOpen = false },
                onAllergiesClick = {
                    menuOpen = false
                    showAllergies = true
                },
            )
        },
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
            if (searchActive) {
                SearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Sök hushåll, personer, taggar eller allergier",
                )
            }
            when {
                state.isLoading -> LoadingState()
                state.errorMessage != null -> ErrorState(state.errorMessage!!, viewModel::refresh)
                else -> {
                    val filtered = state.households
                        .applyFilter(state.filter)
                        .filterBySearch(searchQuery)
                    if (filtered.isEmpty()) {
                        EmptyState(state.filter, searchQuery)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 104.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            item { GuestOverview(state.households) }
                            item { FilterRow(state.households, state.filter, viewModel::setFilter) }
                            items(filtered, key = { it.id }) { household ->
                                HouseholdRow(household, onClick = { onHouseholdClick(household.id) })
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAllergies) {
        AllergyDialog(
            households = state.households,
            onDismiss = { showAllergies = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenTopBar(
    onSearchClick: () -> Unit,
    menuOpen: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onAllergiesClick: () -> Unit,
) {
    TopAppBar(
        title = {},
        actions = {
            Surface(
                modifier = Modifier.padding(end = 8.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            ) {
                IconButton(onClick = onSearchClick) {
                    Icon(Icons.Default.Search, contentDescription = "Sök")
                }
            }
            Surface(
                modifier = Modifier.padding(end = 12.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
            ) {
                Box {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.MoreHoriz, contentDescription = "Mer")
                    }
                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = onDismissMenu,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Allergier") },
                            onClick = onAllergiesClick,
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
            actionIconContentColor = MaterialTheme.colorScheme.onBackground,
        ),
    )
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        placeholder = { Text(placeholder) },
    )
}

@Composable
private fun FilterRow(
    households: List<Household>,
    selected: HouseholdFilter,
    onSelect: (HouseholdFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        HouseholdFilter.values().forEach { filter ->
            FilterPill(
                modifier = Modifier.weight(1f),
                label = filter.label(),
                count = households.applyFilter(filter).sumOf { it.members.size },
                selected = selected == filter,
                onClick = { onSelect(filter) },
            )
        }
    }
}

private fun HouseholdFilter.label(): String = when (this) {
    HouseholdFilter.All -> "Alla"
    HouseholdFilter.Pending -> "Väntar"
    HouseholdFilter.Responded -> "Svarat"
}

@Composable
private fun FilterPill(
    modifier: Modifier,
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = CircleShape,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant,
        ),
    ) {
        Text(
            "$label · $count",
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun HouseholdRow(household: Household, onClick: () -> Unit) {
    val attendingCount = household.members.count { it.rsvpStatus == RsvpStatus.Attending }
    val totalCount = household.members.size
    val pendingCount = household.members.count { it.rsvpStatus == RsvpStatus.Pending }
    val osaStatus = household.osaStatus()
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(household.displayName, style = MaterialTheme.typography.titleMedium)
                    StatusBadge(
                        when (osaStatus) {
                            HouseholdOsaStatus.NotAnswered -> "OSA ej svarad"
                            HouseholdOsaStatus.Answered -> "$attendingCount kommer"
                            HouseholdOsaStatus.Incomplete -> "Ofullständig"
                        },
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    household.members.forEach { member ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                InitialsBadge(member.name, member.rsvpStatus)
                                Text(
                                    member.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            if (osaStatus != HouseholdOsaStatus.NotAnswered) {
                                Text(
                                    member.rsvpStatus.label().lowercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = member.rsvpStatus.statusTextColor(),
                                )
                            }
                        }
                    }
                }
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
                if (household.tags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TagRow(household.tags)
                        Text(
                            when (osaStatus) {
                                HouseholdOsaStatus.NotAnswered -> "inbjudan skapad"
                                HouseholdOsaStatus.Incomplete -> "ofullständig OSA"
                                HouseholdOsaStatus.Answered -> "OSA klar"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
        }
    }
}

@Composable
private fun GuestOverview(households: List<Household>) {
    val guestCount = households.sumOf { it.members.size }
    val attendingCount = households.sumOf { household ->
        household.members.count { it.rsvpStatus == RsvpStatus.Attending }
    }
    val completeHouseholds = households.count { it.osaStatus() == HouseholdOsaStatus.Answered }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("5 JUNI 2027 · LÖRDAG", style = MaterialTheme.typography.labelLarge)
                Text("Gästläge", style = MaterialTheme.typography.headlineSmall)
            }
            androidx.compose.material3.HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                OverviewMetric("Hushåll", households.size.toString())
                OverviewMetric("Gäster", guestCount.toString())
                OverviewMetric("Kommer", attendingCount.toString())
                OverviewMetric("OSA klar", "$completeHouseholds/${households.size}")
            }
        }
    }
}

@Composable
private fun OverviewMetric(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun StatusBadge(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun InitialsBadge(name: String, status: RsvpStatus) {
    Surface(
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = status.initialsBackground(),
        contentColor = status.initialsForeground(),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(initials(name), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        tags.forEach { tag ->
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    tag,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun initials(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")

@Composable
private fun RsvpStatus.statusTextColor() = when (this) {
    RsvpStatus.Attending -> Color(0xFF3C7B29)
    RsvpStatus.Declined -> MaterialTheme.colorScheme.onSurfaceVariant
    RsvpStatus.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
}

@Composable
private fun RsvpStatus.initialsBackground() = when (this) {
    RsvpStatus.Attending -> Color(0xFFD8F0CD)
    RsvpStatus.Declined -> MaterialTheme.colorScheme.surfaceVariant
    RsvpStatus.Pending -> MaterialTheme.colorScheme.surfaceVariant
}

@Composable
private fun RsvpStatus.initialsForeground() = when (this) {
    RsvpStatus.Attending -> Color(0xFF3C7B29)
    RsvpStatus.Declined -> MaterialTheme.colorScheme.onSurfaceVariant
    RsvpStatus.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
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
private fun EmptyState(filter: HouseholdFilter, searchQuery: String) {
    val message = if (searchQuery.isNotBlank()) {
        "Inga gäster matchar sökningen."
    } else {
        when (filter) {
            HouseholdFilter.All -> "Inga hushåll än. Tryck + för att lägga till."
            HouseholdFilter.Pending -> "Alla hushåll har komplett OSA."
            HouseholdFilter.Responded -> "Inga hushåll har komplett OSA ännu."
        }
    }
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AllergyDialog(households: List<Household>, onDismiss: () -> Unit) {
    val allergies = households.flatMap { household ->
        household.members.mapNotNull { member ->
            member.diet
                ?.takeIf { it.isNotBlank() }
                ?.let { AllergyRow(household.displayName, member.name, it) }
        }
    }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allergier") },
        text = {
            if (allergies.isEmpty()) {
                Text("Inga allergier eller kostpreferenser är ifyllda.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    allergies.forEach { row ->
                        Column {
                            Text(row.guestName, style = MaterialTheme.typography.labelLarge)
                            Text(
                                "${row.diet} · ${row.householdName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Stäng") }
        },
    )
}

private data class AllergyRow(
    val householdName: String,
    val guestName: String,
    val diet: String,
)

private enum class HouseholdOsaStatus {
    NotAnswered,
    Incomplete,
    Answered,
}

private fun Household.osaStatus(): HouseholdOsaStatus {
    val selectedCount = members.count { it.rsvpStatus != RsvpStatus.Pending }
    return when {
        selectedCount == 0 -> HouseholdOsaStatus.NotAnswered
        selectedCount == members.size -> HouseholdOsaStatus.Answered
        else -> HouseholdOsaStatus.Incomplete
    }
}

private fun List<Household>.filterBySearch(query: String): List<Household> {
    val normalized = query.trim().lowercase()
    if (normalized.isBlank()) return this
    return filter { household ->
        household.displayName.contains(normalized, ignoreCase = true) ||
            household.tags.any { it.contains(normalized, ignoreCase = true) } ||
            household.members.any { member ->
                member.name.contains(normalized, ignoreCase = true) ||
                    member.diet.orEmpty().contains(normalized, ignoreCase = true) ||
                    member.notes.orEmpty().contains(normalized, ignoreCase = true)
            }
    }
}
