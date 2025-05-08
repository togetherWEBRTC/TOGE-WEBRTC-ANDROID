package example.beechang.together.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import example.beechang.together.ui.call.callNavGraph
import example.beechang.together.ui.home.HomeNavDestination
import example.beechang.together.ui.home.homeNavGraph
import example.beechang.together.ui.user.userNavGraph


@Composable
fun MainNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    NavHost(
        navController = navController,
        startDestination = HomeNavDestination.HOME
    ) {
        homeNavGraph(
            coroutineScope = coroutineScope,
            navController = navController,
        )

        userNavGraph(
            coroutineScope = coroutineScope,
            navController = navController,
        )

        callNavGraph(
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }
}