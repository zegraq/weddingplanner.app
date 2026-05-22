package app.weddingplanner.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()
    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    TopAction(Icons.Default.Search, "Sök") { searchActive = !searchActive }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground,
                ),
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else -> HomeContent(
                    state = state,
                    searchActive = searchActive,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onRetry = viewModel::refresh,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    searchActive: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CountdownCard(state) }
        if (searchActive) {
            item {
                SearchField(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    placeholder = "Sök gäster, allergier eller budget",
                )
            }
        }
        if (searchQuery.isNotBlank()) {
            item { HomeSearchResults(state, searchQuery) }
        }
        if (state.errorMessage != null) {
            item {
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(state.errorMessage)
                        TextButton(onClick = onRetry) { Text("Försök igen") }
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Group,
                    label = "Gäster",
                    primary = "${state.attendingCount}/${state.guestCount}",
                    secondary = "${state.pendingHouseholds} hushåll saknar komplett OSA",
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Payments,
                    label = "Budget",
                    primary = formatSek(state.budget?.paidTotal ?: 0L),
                    secondary = state.budget?.let { "Planerat ${formatSek(it.plannedTotal)}" } ?: "Ej hämtad",
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.CheckCircle,
                    label = "OSA",
                    primary = if (state.pendingHouseholds == 0) "Klar" else "${state.pendingHouseholds} öppna",
                    secondary = "Ett svar per hushåll",
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.TaskAlt,
                    label = "Att-göra",
                    primary = "Nästa",
                    secondary = "Kommer i nästa ticket",
                )
            }
        }
        item { BudgetStatusCard(state) }
    }
}

@Composable
private fun CountdownCard(state: HomeUiState) {
    val daysText = when (val days = state.daysLeft) {
        null -> "Datum saknas"
        0L -> "Idag"
        1L -> "1 dag kvar"
        else -> "$days dagar kvar"
    }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer,
                        ),
                    ),
                )
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.54f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Event, contentDescription = null)
                        Text(formatDate(state.wedding?.date), style = MaterialTheme.typography.labelLarge)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(daysText, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Bröllopet närmar sig. Här är läget just nu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    primary: String,
    secondary: String,
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(primary, style = MaterialTheme.typography.titleLarge)
                Text(secondary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun TopAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.padding(end = 8.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
    ) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(22.dp))
        }
    }
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
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text(placeholder) },
    )
}

@Composable
private fun HomeSearchResults(state: HomeUiState, query: String) {
    val guestMatches = state.households.flatMap { household ->
        household.members.mapNotNull { member ->
            val haystack = listOf(
                household.displayName,
                member.name,
                member.diet.orEmpty(),
                member.notes.orEmpty(),
                household.tags.joinToString(" "),
            ).joinToString(" ")
            if (haystack.contains(query, ignoreCase = true)) {
                "${member.name} · ${household.displayName}"
            } else {
                null
            }
        }
    }
    val budgetMatches = state.budget?.categories.orEmpty().flatMap { category ->
        val categoryMatch = if (
            category.name.contains(query, ignoreCase = true) ||
            category.notes.orEmpty().contains(query, ignoreCase = true)
        ) {
            listOf("${category.name} · budget")
        } else {
            emptyList()
        }
        categoryMatch + category.items.mapNotNull { item ->
            val haystack = listOf(item.description, item.notes.orEmpty(), item.paidAt.orEmpty()).joinToString(" ")
            if (haystack.contains(query, ignoreCase = true)) "${item.description} · ${category.name}" else null
        }
    }
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Sökresultat", style = MaterialTheme.typography.titleMedium)
            if (guestMatches.isEmpty() && budgetMatches.isEmpty()) {
                Text("Inga träffar.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                (guestMatches + budgetMatches).take(8).forEach { result ->
                    Text(result, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun BudgetStatusCard(state: HomeUiState) {
    val budget = state.budget ?: return
    val remaining = budget.unallocated
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Budgetläge", style = MaterialTheme.typography.titleMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tak", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatSek(budget.totalCap))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Planerat", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatSek(budget.plannedTotal))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if ((remaining ?: 0L) >= 0) "Kvar att fördela" else "Över taket", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    formatSek(remaining),
                    color = if ((remaining ?: 0L) < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

private fun formatDate(date: String?): String {
    if (date == null) return "Datum saknas"
    return runCatching {
        LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("sv", "SE")))
    }.getOrDefault(date)
}

private fun formatSek(amount: Long?): String {
    if (amount == null) return "-"
    val grouped = kotlin.math.abs(amount).toString()
        .reversed()
        .chunked(3)
        .joinToString(" ")
        .reversed()
    val sign = if (amount < 0) "-" else ""
    return "$sign$grouped kr"
}
