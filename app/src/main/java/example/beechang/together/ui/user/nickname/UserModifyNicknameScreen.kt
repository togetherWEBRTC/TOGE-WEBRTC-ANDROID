package example.beechang.together.ui.user.nickname

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.CoroutineScope

@Composable
fun UserModifyNicknameRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {
    val userModifyNicknameViewModel: UserModifyNicknameViewModel = hiltViewModel()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val state by userModifyNicknameViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userModifyNicknameViewModel) {
        userModifyNicknameViewModel.sideEffect.collect {
            when (it) {
                UserModifyNicknameEffect.SuccessModifyNickname -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(userModifyNicknameViewModel) {
        userModifyNicknameViewModel.errorEffect.collect { error ->
            if (error is TogeError.NetworkError) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connection_problem),
                    duration = SnackbarDuration.Short
                )
            } else if (error is TogeError.FailModifyNickname) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.occur_error_please_retry),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    UserModifyNicknameScreen(
        modifier = modifier,
        /* STATE */
        state = state,
        snackbarHostState = snackbarHostState,
        /* EVENT */
        onEventNicknameChanged = {
            userModifyNicknameViewModel.onEvent(UserModifyNicknameEvent.NicknameChanged(it))
        },
        onEventSubmit = {
            userModifyNicknameViewModel.onEvent(UserModifyNicknameEvent.SubmitNickname)
        },
        /* NAVIGATION */
        onCloseClick = { navController.navigateUp() }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserModifyNicknameScreen(
    modifier: Modifier = Modifier,
    /* STATE */
    state: UserModifyNicknameState = UserModifyNicknameState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* EVENT */
    onEventNicknameChanged: (String) -> Unit = {},
    onEventSubmit: () -> Unit = {},
    /* NAVIGATION */
    onCloseClick: () -> Unit = {},
) {
    val scrollState = rememberScrollState()

    TogeScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TogeCloseTopBar(onCloseClick = { onCloseClick() })
        },
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = state.isLoading,
    ) {
        val localFocusManager = LocalFocusManager.current

        Column(modifier = Modifier.fillMaxSize()) {

            CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectTapGestures(onTap = {
                                localFocusManager.clearFocus()
                            })
                        }
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp),
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(id = R.string.nickname_change_confirmation),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    TogeOutLineTextField(
                        value = state.nickname,
                        onValueChange = { onEventNicknameChanged(it) },
                        labelText = stringResource(R.string.nickname),
                        placeholderText = stringResource(R.string.input_nickname),
                        singleLine = true,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        onImeAction = { onEventSubmit() },
                        focusManager = localFocusManager,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            TogeConfirmButton(
                text = stringResource(R.string.action_change),
                onClick = { onEventSubmit() },
                enabled = state.isNicknameValid && !state.isLoading,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth()
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun UserModifyNicknameScreenPreview() {
    UserModifyNicknameScreen()
}