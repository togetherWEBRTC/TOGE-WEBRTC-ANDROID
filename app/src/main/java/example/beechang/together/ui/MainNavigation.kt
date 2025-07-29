package example.beechang.together.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import example.beechang.together.TogeAppEvent
import example.beechang.together.TogeAppEventViewModel
import example.beechang.together.ui.call.callNavGraph
import example.beechang.together.ui.call.CallNavDestination
import example.beechang.together.ui.home.HomeNavDestination
import example.beechang.together.ui.home.homeNavGraph
import example.beechang.together.ui.user.userNavGraph


@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    activityClass: Class<out Activity>
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val appEventViewModel: TogeAppEventViewModel = hiltViewModel()
    
    LaunchedEffect(appEventViewModel) {
        appEventViewModel.eventFlow.collect { event ->
            when (event) {
                is TogeAppEvent.StopCalling -> {
                    val currentRoute = navController.currentDestination?.route
                    if (currentRoute?.contains("call") == true) {
                        CallNavDestination.actionStopCall(navController)
                    }
                }
            }
        }
    }

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
            activityClass = activityClass
        )
    }
}