package example.beechang.together.ui.call

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import example.beechang.together.ui.call.room.CallRoomRouter
import example.beechang.together.ui.call.waiting.CallWaitingRouter
import kotlinx.coroutines.CoroutineScope

object CallNavDestination {

    private const val CALL_BASE = "call"
    const val CALL_ROOM = "$CALL_BASE?roomCode={roomCode}&isHost={isHost}"
    const val CALL_WAITING = "$CALL_BASE/waiting?roomCode={roomCode}"

    fun navigateToCall(navController: NavController, roomCode: String, isHost: Boolean) {
        if (isHost) {
            // 방 생성자는 바로 통화방으로 이동
            navController.navigate("$CALL_BASE?roomCode=$roomCode&isHost=true") {
                launchSingleTop = true
            }
        } else {
            // 참여자는 대기 화면으로 이동
            navController.navigate("$CALL_BASE/waiting?roomCode=$roomCode") {
                launchSingleTop = true
            }
        }
    }

    fun navigateToCallFromWaiting(navController: NavController, roomCode: String) {
        navController.navigate("$CALL_BASE?roomCode=$roomCode&isHost=false") {
            popUpTo("$CALL_BASE/waiting") {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
}

fun NavGraphBuilder.callNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavController,
) {
    composable(
        route = CallNavDestination.CALL_ROOM,
        arguments = listOf(
            navArgument("roomCode") {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument("isHost") {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    ) { navBackStackEntry ->
        CallRoomRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }

    composable  (
        route = CallNavDestination.CALL_WAITING,
        arguments = listOf(
            navArgument("roomCode") {
                type = NavType.StringType
                defaultValue = ""
            }
        )
    ) { navBackStackEntry ->
        CallWaitingRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }
}