package example.beechang.together.ui.user.signup

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.ui.component.button.TogeConfirmButton
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.text.TogeOutLineTextField
import example.beechang.together.ui.component.topbar.TogeSimpleBackTopBar
import example.beechang.together.ui.theme.LocalTogeAppColor
import example.beechang.together.ui.user.UserNavDestination
import kotlinx.coroutines.launch

@Composable
fun SignUpRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {

    val signUpViewModel: SignUpViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        signUpViewModel.errorEffect.collect { error ->
            if (error is TogeError.DuplicatedId) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.input_id_duplicate),
                        duration = SnackbarDuration.Short
                    )
                }
            } else if (error is TogeError.NetworkError) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connection_problem),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        signUpViewModel.sideEffect.collect { result ->
            when (result) {
                SignUpEffect.SuccessSignUp -> { // success sign up -> move to welcome screen
                    UserNavDestination.navigateToWelcomeFromSignup(
                        navController = navController,
                        nickname = signUpViewModel.uiState.value.nickname
                    )
                }
            }
        }
    }

    val state by signUpViewModel.uiState.collectAsStateWithLifecycle()

    SignUpScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        /* EVENT */
        onEventUserIdChanged = { userId ->
            signUpViewModel.onEvent(SignUpEvent.UserIdChanged(userId))
        },
        onEventNicknameChanged = { nickname ->
            signUpViewModel.onEvent(SignUpEvent.NicknameChanged(nickname))
        },
        onEventPasswordChanged = { password ->
            signUpViewModel.onEvent(SignUpEvent.PasswordChanged(password))
        },
        onEventConfirmPasswordChanged = { confirmPassword ->
            signUpViewModel.onEvent(SignUpEvent.ConfirmPasswordChanged(confirmPassword))
        },
        onEventCheckUserId = {
            signUpViewModel.onEvent(SignUpEvent.CheckUserId)
        },
        onEventSubmitSignUp = {
            signUpViewModel.onEvent(SignUpEvent.SubmitSignUp)
        },
        /* NAVIGATION */
        onClickBack = { navController.navigateUp() }
    )
}

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    /* STATE */
    state: SignUpUiState = SignUpUiState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* EVENT */
    onEventUserIdChanged: (String) -> Unit = {},
    onEventNicknameChanged: (String) -> Unit = {},
    onEventPasswordChanged: (String) -> Unit = {},
    onEventConfirmPasswordChanged: (String) -> Unit = {},
    onEventCheckUserId: () -> Unit = {},
    onEventSubmitSignUp: () -> Unit = {},
    /* NAVIGATION */
    onClickBack: () -> Unit = {}
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
        topBar = { TogeSimpleBackTopBar(onClickBack = onClickBack) },
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = state.isLoading
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.signup_prompt),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(32.dp)) // User ID
            SignUpUserIdRow(
                state = state,
                onUserIdChanged = onEventUserIdChanged,
                onCheckUserId = onEventCheckUserId,
                localFocusManager = localFocusManager
            )

            Spacer(modifier = Modifier.height(4.dp)) // Nick Name
            TogeOutLineTextField(
                value = state.nickname,
                onValueChange = { onEventNicknameChanged(it) },
                labelText = stringResource(R.string.nickname),
                placeholderText = stringResource(R.string.input_nickname),
                singleLine = true,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onImeAction = {},
                focusManager = localFocusManager,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // Password
            TogeOutLineTextField(
                value = state.password,
                onValueChange = { onEventPasswordChanged(it) },
                labelText = stringResource(R.string.password),
                placeholderText = stringResource(R.string.input_password),
                singleLine = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                onImeAction = {},
                isPassword = true,
                focusManager = localFocusManager,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp)) // Password Confirm
            TogeOutLineTextField(
                value = state.passwordConfirm,
                onValueChange = { onEventConfirmPasswordChanged(it) },
                labelText = stringResource(R.string.password_confirm),
                placeholderText = stringResource(R.string.input_password_confirm),
                singleLine = true,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = { onEventSubmitSignUp() },
                isPassword = true,
                focusManager = localFocusManager,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp)) // Sign Up Button
            TogeConfirmButton(
                text = stringResource(R.string.signup),
                onClick = { onEventSubmitSignUp() },
                enabled = state.isSignUpEnabled && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SignUpUserIdRow(
    state: SignUpUiState,
    onUserIdChanged: (String) -> Unit,
    onCheckUserId: () -> Unit,
    localFocusManager: FocusManager
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TogeOutLineTextField(
            value = state.userId,
            onValueChange = { onUserIdChanged(it) },
            labelText = stringResource(R.string.id),
            placeholderText = stringResource(R.string.input_id),
            singleLine = true,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            onImeAction = {},
            focusManager = localFocusManager,
            modifier = Modifier.weight(1f)
        )

        TogeConfirmButton(
            text = stringResource(R.string.check_id),
            onClick = { onCheckUserId() },
            enabled = state.userId.isNotEmpty() && !state.isLoading,
            modifier = Modifier
                .width(96.dp)
                .padding(top = 8.dp) // 8dp 사용한 이유 : Material3 텍스트필드는 라벨이 올라가는 공간을 점유해서 중앙 정렬이 제대로 작동하지 않게 됨.
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        modifier = Modifier
            .fillMaxWidth(),
        text = if (state.isUsableId) {
            stringResource(R.string.usable_id)
        } else if (state.isDuplicateId) {
            stringResource(R.string.input_id_duplicate)
        } else {
            ""
        },
        style = MaterialTheme.typography.bodySmall,
        color = if (state.isUsableId) LocalTogeAppColor.current.success400 else LocalTogeAppColor.current.crimson400,
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewSignUpScreen() {
    SignUpScreen(
        state = SignUpUiState(
            isLoading = false,
            userId = "TogeUserId",
            nickname = "TOGE_NICKNAME",
            password = "password123",
            passwordConfirm = "password123",
            isUsableId = true,
            isDuplicateId = false,
            isSignUpEnabled = true
        )
    )
}