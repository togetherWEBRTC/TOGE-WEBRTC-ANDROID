package example.beechang.together.ui.user

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import example.beechang.together.ui.user.login.UserLoginRouter
import example.beechang.together.ui.user.mypage.UserMyPageRouter
import example.beechang.together.ui.user.nickname.UserModifyNicknameRouter
import example.beechang.together.ui.user.signup.SignUpRouter
import example.beechang.together.ui.user.signup.UserSocialSignUpRouter
import example.beechang.together.ui.user.welcome.WelcomeRouter
import kotlinx.coroutines.CoroutineScope

object UserNavDestination {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val WELCOME = "welcome?nickname={nickname}"
    const val MYPAGE = "mypage"
    const val SOCIAL_SIGNUP = "social_signup?token={token}"
    const val MODIFY_NICKNAME = "modify_nickname"

    fun welcomeWithArgs(nickname: String): String {
        return "welcome?nickname=$nickname"
    }

    fun socialSignupWithArgs(token: String): String {
        return "social_signup?token=$token"
    }

    fun navigateToWelcomeFromSignup(navController: NavController, nickname: String) {
        navController.navigate(welcomeWithArgs(nickname)) {
            popUpTo(SIGNUP) { inclusive = true }
        }
    }

    fun navigateToLoginFromWelcome(navController: NavController) {
        navController.navigate(LOGIN) {
            popUpTo(WELCOME) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    fun navigateToSocialSignup(
        navController: NavController,
        token: String,
    ) {
        navController.navigate(socialSignupWithArgs(token))
    }

    fun navigationToModifyNickname(navController: NavController) {
        navController.navigate(MODIFY_NICKNAME)
    }

    fun naviagetToWelcomeFromSocialSignup(navController: NavController, nickname: String) {
        navController.navigate(welcomeWithArgs(nickname)) {
            popUpTo(SOCIAL_SIGNUP) { inclusive = true }
        }
    }

    fun navigateToPreLogin(navController: NavController) {
        navController.popBackStack(route = LOGIN, inclusive = true)
    }

    fun navigateToLogin(
        navController: NavController,
        removeCurrentFromStack: Boolean = false,
    ) {
        navController.navigate(LOGIN) {
            if (removeCurrentFromStack) {
                navController.currentBackStackEntry?.destination?.route?.let {
                    popUpTo(it) { inclusive = true }
                }
            }
            launchSingleTop = true
        }
    }
}


fun NavGraphBuilder.userNavGraph(
    coroutineScope: CoroutineScope,
    navController: NavController,
) {
    composable(UserNavDestination.LOGIN) { navBackStackEntry ->
        UserLoginRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }

    composable(UserNavDestination.SIGNUP) { navBackStackEntry ->
        SignUpRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }

    composable(
        route = UserNavDestination.SOCIAL_SIGNUP,
        arguments = listOf(
            navArgument("token") {
                type = NavType.StringType
                defaultValue = ""
                nullable = false
            }
        )
    ) { navBackStackEntry ->
        UserSocialSignUpRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }

    composable(
        route = UserNavDestination.MODIFY_NICKNAME,
    ) { navBackStackEntry ->
        UserModifyNicknameRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }

    composable(
        route = UserNavDestination.WELCOME,
        arguments = listOf(
            navArgument("nickname") {
                type = NavType.StringType
                defaultValue = ""
                nullable = false
            }
        )
    ) { navBackStackEntry ->
        WelcomeRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }

    composable(UserNavDestination.MYPAGE) { navBackStackEntry ->
        UserMyPageRouter(
            navBackStackEntry = navBackStackEntry,
            coroutineScope = coroutineScope,
            navController = navController,
        )
    }
}