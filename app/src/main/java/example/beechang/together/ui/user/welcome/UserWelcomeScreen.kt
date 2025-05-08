package example.beechang.together.ui.user.welcome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import example.beechang.together.R
import example.beechang.together.ui.component.button.TogeConfirmButton
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.topbar.TogeSimpleBackTopBar
import example.beechang.together.ui.component.util.LottieWelcome
import example.beechang.together.ui.user.UserNavDestination
import kotlinx.coroutines.CoroutineScope


@Composable
fun WelcomeRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {
    val nickname = navBackStackEntry.arguments?.getString("nickname") ?: ""

    WelcomeScreen(
        modifier = modifier,
        nickname = nickname,
        onClickMoveToLogin = {
            UserNavDestination.navigateToLoginFromWelcome(navController = navController)
        },
    )
}


@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    nickname: String = "",
    onClickMoveToLogin: () -> Unit,
) {
    val scrollState = rememberScrollState()

    TogeScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TogeSimpleBackTopBar(onClickBack = onClickMoveToLogin) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                LottieWelcome(
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = nickname + stringResource(id = R.string.welcome),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

            }

            TogeConfirmButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.go_login),
                onClick = onClickMoveToLogin,
            )
        }
    }
}

@Preview
@Composable
fun PreviewWelcomeScreen() {
    WelcomeScreen(
        nickname = "HelloWorld",
        onClickMoveToLogin = {}
    )
}