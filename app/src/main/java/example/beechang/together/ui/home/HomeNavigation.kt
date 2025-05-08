package example.beechang.together.ui.home

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.CoroutineScope


object HomeNavDestination {
    const val HOME = "home"

    fun navigateToHome(navController: NavController) {
        navController.navigate(HOME) {
            popUpTo(navController.graph.id) { inclusive = true }
            launchSingleTop = true
        }
    }
}

fun NavGraphBuilder.homeNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavController,

    ) {
    composable(HomeNavDestination.HOME) { navBackStackEntry ->
        HomeRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }
}