package app.weddingplanner.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.weddingplanner.api.ApiClient
import app.weddingplanner.ui.budget.BudgetScreen
import app.weddingplanner.ui.budget.BudgetViewModel
import app.weddingplanner.ui.guests.GuestListScreen
import app.weddingplanner.ui.guests.GuestListViewModel
import app.weddingplanner.ui.guests.HouseholdDetailScreen
import app.weddingplanner.ui.guests.HouseholdDetailViewModel
import app.weddingplanner.ui.guests.HouseholdEditScreen
import app.weddingplanner.ui.guests.HouseholdEditViewModel

private object Routes {
    const val GUESTS = "guests"
    const val GUEST_NEW = "guests/new"
    const val GUEST_DETAIL = "guests/{id}"
    const val GUEST_EDIT = "guests/{id}/edit"
    const val BUDGET = "budget"
    const val TODO = "todo"

    fun guestDetail(id: String) = "guests/$id"
    fun guestEdit(id: String) = "guests/$id/edit"
}

private data class TopTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TopTab(Routes.GUESTS, "Gäster", Icons.Default.People),
    TopTab(Routes.BUDGET, "Budget", Icons.Default.Savings),
    TopTab(Routes.TODO, "Att-göra", Icons.Default.CheckBox),
)

@Composable
fun RootNavigation(apiClient: ApiClient) {
    val nav = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(nav) },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.GUESTS,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Routes.GUESTS) {
                val vm: GuestListViewModel = viewModel(factory = factory(apiClient) {
                    GuestListViewModel(apiClient)
                })
                GuestListScreen(
                    viewModel = vm,
                    onHouseholdClick = { id -> nav.navigate(Routes.guestDetail(id)) },
                    onNewHousehold = { nav.navigate(Routes.GUEST_NEW) },
                )
            }
            composable(Routes.GUEST_NEW) {
                val vm: HouseholdEditViewModel = viewModel(factory = factory(apiClient) {
                    HouseholdEditViewModel(apiClient, householdId = null)
                })
                HouseholdEditScreen(viewModel = vm, isNew = true, onBack = { nav.popBackStack() })
            }
            composable(Routes.GUEST_DETAIL) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                    ?: return@composable
                val vm: HouseholdDetailViewModel = viewModel(
                    key = "detail-$id",
                    factory = factory(apiClient) {
                        HouseholdDetailViewModel(apiClient, id)
                    },
                )
                HouseholdDetailScreen(
                    viewModel = vm,
                    onBack = { nav.popBackStack() },
                    onEdit = { nav.navigate(Routes.guestEdit(it)) },
                )
            }
            composable(Routes.GUEST_EDIT) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                    ?: return@composable
                val vm: HouseholdEditViewModel = viewModel(
                    key = "edit-$id",
                    factory = factory(apiClient) {
                        HouseholdEditViewModel(apiClient, householdId = id)
                    },
                )
                HouseholdEditScreen(viewModel = vm, isNew = false, onBack = { nav.popBackStack() })
            }
            composable(Routes.BUDGET) {
                val vm: BudgetViewModel = viewModel(factory = factory(apiClient) {
                    BudgetViewModel(apiClient)
                })
                BudgetScreen(viewModel = vm)
            }
            composable(Routes.TODO) { PlaceholderScreen("Att-göra") }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    NavigationBar {
        tabs.forEach { tab ->
            val selected = current?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    nav.navigate(tab.route) {
                        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            "$title — kommer i nästa ticket",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private inline fun <reified VM : androidx.lifecycle.ViewModel> factory(
    apiClient: ApiClient,
    crossinline create: () -> VM,
) = viewModelFactory {
    initializer { create() }
}
