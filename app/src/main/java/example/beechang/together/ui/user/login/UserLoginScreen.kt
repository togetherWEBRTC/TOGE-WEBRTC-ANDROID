package example.beechang.together.ui.user.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.ui.component.button.TogeConfirmButton
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.text.TogeOutLineTextField
import example.beechang.together.ui.component.topbar.TogeCloseTopBar
import example.beechang.together.ui.user.UserNavDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun UserLoginRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {

    val userLoginViewModel: UserLoginViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(userLoginViewModel) {
        userLoginViewModel.sideEffect.collect {
            if (it is UserLoginEffect.LoginSuccess) {
                navController.navigateUp()
            }
        }
    }

    LaunchedEffect(userLoginViewModel) {
        userLoginViewModel.errorEffect.collect {
            if (it is TogeError.FailedLogin) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.fail_login),
                        duration = SnackbarDuration.Short
                    )
                }
            } else if (it is TogeError.NetworkError) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connection_problem),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val state by userLoginViewModel.uiState.collectAsStateWithLifecycle()

    UserLoginScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        /* EVENT */
        onEventUserIdChanged = { userId ->
            userLoginViewModel.onEvent(UserLoginEvent.UserLoginIdChanged(userId))
        },
        onEventPasswordChanged = { password ->
            userLoginViewModel.onEvent(UserLoginEvent.PasswordChanged(password))
        },
        onEventLogin = {
            userLoginViewModel.onEvent(UserLoginEvent.Login(state.inputUserId, state.inputPassword))
        },
        /* NAVIGATION */
        onCloseScreen = { navController.navigateUp() },
        onMoveToSignUp = { navController.navigate(UserNavDestination.SIGNUP) }
    )
}

@Composable
fun UserLoginScreen(
    modifier: Modifier = Modifier,
    /* STATE */
    state: UserLoginState = UserLoginState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* EVENT */
    onEventUserIdChanged: (String) -> Unit = {},
    onEventPasswordChanged: (String) -> Unit = {},
    onEventLogin: () -> Unit = {},
    /* NAVIGATION */
    onCloseScreen: () -> Unit = {},
    onMoveToSignUp: () -> Unit = {}
) {
    val localFocusManager = LocalFocusManager.current

    TogeScaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus() // Use for clearFocus to id , password input
                })
            },
        topBar = { TogeCloseTopBar(onCloseClick = onCloseScreen) },
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = state.isLoading,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(id = R.string.together),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(32.dp))
            TogeOutLineTextField(
                value = state.inputUserId,
                onValueChange = { onEventUserIdChanged(it) },
                labelText = stringResource(R.string.id),
                placeholderText = stringResource(R.string.input_id),
                singleLine = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onImeAction = {},
                focusManager = localFocusManager,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            TogeOutLineTextField(
                value = state.inputPassword,
                onValueChange = { onEventPasswordChanged(it) },
                labelText = stringResource(R.string.password),
                placeholderText = stringResource(R.string.input_password),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                singleLine = true,
                isPassword = true,
                onImeAction = {
                    localFocusManager.clearFocus()
                    onEventLogin()
                },
                focusManager = localFocusManager,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
            TogeConfirmButton(
                text = stringResource(R.string.login),
                onClick = onEventLogin,
                enabled = state.isLoginButtonEnabled,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.restart_prompt),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = " ",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.signup_prompt),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier
                        .clickable {
                            onMoveToSignUp()
                        },
                )
            }
        }
    }
}

@Preview()
@Composable
fun PreviewUserLoginScreen() {
    UserLoginScreen(
        state = UserLoginState(
            isLoading = false,
            inputUserId = "test_user",
            inputPassword = "password123",
        )
    )
}