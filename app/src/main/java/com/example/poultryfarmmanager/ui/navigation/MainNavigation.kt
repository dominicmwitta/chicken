package com.example.poultryfarmmanager.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import com.example.poultryfarmmanager.ui.theme.LocalAppStrings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.poultryfarmmanager.ui.screens.birds.BirdsScreen
import com.example.poultryfarmmanager.ui.screens.dashboard.DashboardScreen
import com.example.poultryfarmmanager.ui.screens.eggs.EggsScreen
import com.example.poultryfarmmanager.ui.screens.feed.FeedScreen
import com.example.poultryfarmmanager.ui.screens.health.HealthScreen
import com.example.poultryfarmmanager.ui.screens.setup.SetupScreen
import com.example.poultryfarmmanager.ui.screens.setup.SetupViewModel
import com.example.poultryfarmmanager.ui.screens.tasks.TasksScreen

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : Screen("dashboard", "", Icons.Filled.Home,            Icons.Outlined.Home)
    object Birds     : Screen("birds",     "", Icons.Filled.Fastfood,        Icons.Outlined.Fastfood)
    object Eggs      : Screen("eggs",      "", Icons.Filled.Egg,             Icons.Outlined.Egg)
    object Feed      : Screen("feed",      "", Icons.Filled.ShoppingCart,    Icons.Outlined.ShoppingCart)
    object Health    : Screen("health",    "", Icons.Filled.HealthAndSafety, Icons.Outlined.HealthAndSafety)
    object Tasks     : Screen("tasks",     "", Icons.Filled.CheckCircle,     Icons.Outlined.CheckCircle)
}

private val bottomNavItems = listOf(
    Screen.Dashboard, Screen.Birds, Screen.Eggs, Screen.Feed, Screen.Health, Screen.Tasks
)

@Composable
fun MainNavigation() {
    val setupViewModel: SetupViewModel = hiltViewModel()
    val rootNav = rememberNavController()
    val startDest = if (setupViewModel.hasFarmId()) "farm" else "setup"

    NavHost(navController = rootNav, startDestination = startDest) {
        composable("setup") {
            SetupScreen(onComplete = {
                rootNav.navigate("farm") { popUpTo("setup") { inclusive = true } }
            })
        }
        composable("farm") { FarmNavHost() }
    }
}

@Composable
private fun FarmNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FloatingBottomNav(
                items = bottomNavItems,
                currentDestination = currentDestination,
                onNavigate = { screen ->
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        val showAddArg = listOf(navArgument("showAdd") { type = NavType.BoolType; defaultValue = false })
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(onNavigateTo = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = false
                    }
                })
            }
            composable("birds?showAdd={showAdd}", arguments = showAddArg) { entry ->
                BirdsScreen(showAdd = entry.arguments?.getBoolean("showAdd") == true)
            }
            composable("eggs?showAdd={showAdd}", arguments = showAddArg) { entry ->
                EggsScreen(showAdd = entry.arguments?.getBoolean("showAdd") == true)
            }
            composable("feed?showAdd={showAdd}", arguments = showAddArg) { entry ->
                FeedScreen(showAdd = entry.arguments?.getBoolean("showAdd") == true)
            }
            composable("health?showAdd={showAdd}", arguments = showAddArg) { entry ->
                HealthScreen(showAdd = entry.arguments?.getBoolean("showAdd") == true)
            }
            composable("tasks?showAdd={showAdd}", arguments = showAddArg) { entry ->
                TasksScreen(showAdd = entry.arguments?.getBoolean("showAdd") == true)
            }
        }
    }
}

@Composable
private fun localTitle(screen: Screen): String {
    val s = LocalAppStrings.current
    return when (screen) {
        Screen.Dashboard -> s.navHome
        Screen.Birds     -> s.navBirds
        Screen.Eggs      -> s.navEggs
        Screen.Feed      -> s.navFeed
        Screen.Health    -> s.navHealth
        Screen.Tasks     -> s.navTasks
        else             -> screen.title
    }
}

@Composable
private fun FloatingBottomNav(
    items: List<Screen>,
    currentDestination: NavDestination?,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            shadowElevation = 20.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.route?.substringBefore("?") == screen.route
                    } == true
                    NavPill(screen = screen, selected = selected, title = localTitle(screen), onClick = { onNavigate(screen) })
                }
            }
        }
    }
}

@Composable
private fun NavPill(screen: Screen, selected: Boolean, title: String, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
        label = "navBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navContent"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(bgColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = if (selected) 14.dp else 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        if (selected) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 11.sp,
                color = contentColor
            )
        }
    }
}
