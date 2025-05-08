package example.beechang.together.ui.user.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.model.LoginState
import example.beechang.together.ui.component.card.TogeProfileItem
import example.beechang.together.ui.component.dialog.TogeDialog
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.topbar.TogeSimpleBackTopBar
import example.beechang.together.ui.component.util.CircularImage
import example.beechang.together.ui.component.util.ClickShrinkEffect
import example.beechang.together.ui.home.HomeNavDestination
import example.beechang.together.ui.theme.LocalTogeAppColor
import example.beechang.together.ui.user.UserNavDestination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun UserMyPageRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {
    val userPageViewModel: UserMyPageViewModel = hiltViewModel()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userPageViewModel) {
        userPageViewModel.sideEffect.collect {
            when (it) {
                UserMyPageEffect.SuccessModifyProfileImage -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.my_profile_edit_success),
                        actionLabel = context.getString(R.string.ok),
                    )
                }
            }
        }
    }

    LaunchedEffect(userPageViewModel) {
        userPageViewModel.errorEffect.collect { error ->
            if (error is TogeError.NetworkError) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connection_problem),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(userPageViewModel) {
        userPageViewModel.loginState.collect { state ->
            if (state == LoginState.Logout) {
                HomeNavDestination.navigateToHome(navController)
            } else if (state == LoginState.SessionExpired) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.login_expired),
                    actionLabel = context.getString(R.string.ok),
                    duration = SnackbarDuration.Short,
                )
                UserNavDestination.navigateToLogin(
                    navController = navController,
                    removeCurrentFromStack = true,
                )
            }
        }
    }

    val state by userPageViewModel.uiState.collectAsStateWithLifecycle()

    UserMyPageScreen(
        modifier = modifier,
        state = state,
        snackbarHostState = snackbarHostState,
        /* EVENT */
        onEventDoubleCheckModifyProfileImage = {
            userPageViewModel.onEvent(UserMyPageEvent.OnDoubleCheckModifyProfileImage)
        },
        onEventModifyProfileImage = {
            userPageViewModel.onEvent(UserMyPageEvent.OnModifyProfileImage)
        },
        onEventCancelModifyProfileImage = {
            userPageViewModel.onEvent(UserMyPageEvent.OnCancelModifyProfileImage)
        },
        onEventLogout = {
            userPageViewModel.onEvent(UserMyPageEvent.OnLogout)
        },
        onClickIsPreparing = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.readey),
                    actionLabel = context.getString(R.string.ok),
                )
            }
        },
        /* NAVIGATION */
        onClickBack = { navController.navigateUp() }
    )
}

@Composable
fun UserMyPageScreen(
    modifier: Modifier = Modifier,
    /* STATE */
    state: UserMyPageState = UserMyPageState(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* EVENT */
    onEventDoubleCheckModifyProfileImage: () -> Unit = {},
    onEventModifyProfileImage: () -> Unit = {},
    onEventCancelModifyProfileImage: () -> Unit = {},
    onEventLogout: () -> Unit = {},
    onClickIsPreparing: () -> Unit = {},
    /* NAVIGATION */
    onClickBack: () -> Unit = {}
) {

    val scrollState = rememberScrollState()

    TogeDialog(
        isShowDialog = state.isShowDoubleCheckModifyProfileImage,
        title = stringResource(R.string.my_profile_edit),
        content = stringResource(R.string.my_profile_edit_prompt),
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        onConfirm = { onEventModifyProfileImage() },
        onDismiss = { onEventCancelModifyProfileImage() },
    )

    TogeScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TogeSimpleBackTopBar(onClickBack = onClickBack) },
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = state.isLoading,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))


            // Profile Image
            ConstraintLayout(
                modifier = Modifier.wrapContentSize()
            ) {
                CircularImage(
                    modifier = Modifier.constrainAs(ref = createRef()) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                    imageUrl = state.profileImageUrl,
                    size = 160.dp,
                    borderWidth = 4.dp,
                )
                ClickShrinkEffect(
                    modifier = Modifier.constrainAs(ref = createRef()) {
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                    onClick = { onEventDoubleCheckModifyProfileImage() }
                ) {
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, LocalTogeAppColor.current.grey999, CircleShape)
                            .background(color = LocalTogeAppColor.current.primary500),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularImage(
                            imageUrl = R.drawable.ic_union,
                            size = 24.dp,
                            borderWidth = 0.dp,
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.welcome_user_nickname, state.nickname),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            TogeProfileItem(
                label = stringResource(R.string.id),
                value = state.userId,
                showChangeButton = false
            )
            Spacer(modifier = Modifier.height(8.dp))

            TogeProfileItem(
                label = stringResource(R.string.nickname),
                value = state.nickname,
                showChangeButton = true,
                onChangeClick = {
                    onClickIsPreparing()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            TogeProfileItem(
                label = stringResource(R.string.password),
                description = stringResource(R.string.password_description),
                showChangeButton = true,
                onChangeClick = {
                    onClickIsPreparing()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.logout),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LocalTogeAppColor.current.grey600,
                    modifier = Modifier.clickable {
                        onEventLogout()
                    }
                )
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserMyPageScreen() {
    UserMyPageScreen(
        state = UserMyPageState(
            isLoading = false,
            profileImageUrl = "https://example.com/profile.jpg",
            nickname = "TOGE_NAME",
            userId = "TogeUserId",
            isShowDoubleCheckModifyProfileImage = false
        )
    )
}