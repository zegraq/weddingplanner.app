package app.weddingplanner.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
import app.weddingplanner.ui.home.HomeScreen
import app.weddingplanner.ui.home.HomeViewModel
import app.weddingplanner.ui.shopping.ShoppingEditScreen
import app.weddingplanner.ui.shopping.ShoppingEditViewModel
import app.weddingplanner.ui.shopping.ShoppingListScreen
import app.weddingplanner.ui.shopping.ShoppingListViewModel
import app.weddingplanner.ui.todo.TodoEditScreen
import app.weddingplanner.ui.todo.TodoEditViewModel
import app.weddingplanner.ui.todo.TodoListScreen
import app.weddingplanner.ui.todo.TodoListViewModel

private object Routes {
    const val HOME = "home"
    const val GUESTS = "guests"
    const val GUEST_NEW = "guests/new"
    const val GUEST_DETAIL = "guests/{id}"
    const val GUEST_EDIT = "guests/{id}/edit"
    const val BUDGET = "budget"
    const val TODO = "todo"
    const val TODO_NEW = "todo/new"
    const val TODO_EDIT = "todo/{id}/edit"
    const val SHOPPING = "shopping"
    const val SHOPPING_NEW = "shopping/new"
    const val SHOPPING_EDIT = "shopping/{id}/edit"

    fun guestDetail(id: String) = "guests/$id"
    fun guestEdit(id: String) = "guests/$id/edit"
    fun todoEdit(id: String) = "todo/$id/edit"
    fun shoppingEdit(id: String) = "shopping/$id/edit"
}

private data class TopTab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TopTab(Routes.HOME, "Hem", Icons.Default.Home),
    TopTab(Routes.GUESTS, "Gäster", Icons.Default.People),
    TopTab(Routes.BUDGET, "Budget", Icons.Default.Savings),
    TopTab(Routes.TODO, "Att-göra", Icons.Default.CheckBox),
    TopTab(Routes.SHOPPING, "Inköp", Icons.Default.ShoppingCart),
)

@Composable
fun RootNavigation(apiClient: ApiClient) {
    val nav = rememberNavController()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = { BottomBar(nav) },
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel(factory = factory(apiClient) {
                    HomeViewModel(apiClient)
                })
                HomeScreen(viewModel = vm)
            }
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
            composable(Routes.TODO) {
                val vm: TodoListViewModel = viewModel(factory = factory(apiClient) {
                    TodoListViewModel(apiClient)
                })
                TodoListScreen(
                    viewModel = vm,
                    onNewTodo = { nav.navigate(Routes.TODO_NEW) },
                    onEditTodo = { id -> nav.navigate(Routes.todoEdit(id)) },
                )
            }
            composable(Routes.TODO_NEW) {
                val vm: TodoEditViewModel = viewModel(factory = factory(apiClient) {
                    TodoEditViewModel(apiClient, todoId = null)
                })
                TodoEditScreen(viewModel = vm, isNew = true, onBack = { nav.popBackStack() })
            }
            composable(Routes.TODO_EDIT) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                    ?: return@composable
                val vm: TodoEditViewModel = viewModel(
                    key = "todo-edit-$id",
                    factory = factory(apiClient) {
                        TodoEditViewModel(apiClient, todoId = id)
                    },
                )
                TodoEditScreen(viewModel = vm, isNew = false, onBack = { nav.popBackStack() })
            }
            composable(Routes.SHOPPING) {
                val vm: ShoppingListViewModel = viewModel(factory = factory(apiClient) {
                    ShoppingListViewModel(apiClient)
                })
                ShoppingListScreen(
                    viewModel = vm,
                    onNewItem = { nav.navigate(Routes.SHOPPING_NEW) },
                    onEditItem = { id -> nav.navigate(Routes.shoppingEdit(id)) },
                )
            }
            composable(Routes.SHOPPING_NEW) {
                val vm: ShoppingEditViewModel = viewModel(factory = factory(apiClient) {
                    ShoppingEditViewModel(apiClient, itemId = null)
                })
                ShoppingEditScreen(viewModel = vm, isNew = true, onBack = { nav.popBackStack() })
            }
            composable(Routes.SHOPPING_EDIT) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")
                    ?: return@composable
                val vm: ShoppingEditViewModel = viewModel(
                    key = "shopping-edit-$id",
                    factory = factory(apiClient) {
                        ShoppingEditViewModel(apiClient, itemId = id)
                    },
                )
                ShoppingEditScreen(viewModel = vm, isNew = false, onBack = { nav.popBackStack() })
            }
        }
    }
}

@Composable
private fun BottomBar(nav: NavHostController) {
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
    ) {
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
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
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
