package example.beechang.together.ui.home

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.model.LoginState
import example.beechang.together.ui.call.CallNavDestination
import example.beechang.together.ui.component.bottombar.HomeBottomBar
import example.beechang.together.ui.component.button.TogeFloatingButtonWithIcon
import example.beechang.together.ui.component.card.TogePermissionItem
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.text.TogeClickableText
import example.beechang.together.ui.component.topbar.HomeTopBar
import example.beechang.together.ui.component.util.CircularImage
import example.beechang.together.ui.component.util.ClickShrinkEffect
import example.beechang.together.ui.theme.LocalTogeAppColor
import example.beechang.together.ui.user.UserNavDestination
import example.beechang.together.ui.utils.PermissionHandlerStatus
import example.beechang.together.ui.utils.rememberMultiPermissionHandler

import kotlinx.coroutines.CoroutineScope

@Composable
fun HomeRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    onMoveToLogin: (() -> Unit)? = {},
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {

    val homeViewModel: HomeViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val loginState by homeViewModel.loginState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(homeViewModel) {
        homeViewModel.errorEffect.collect { error ->
            if (error is TogeError.FailedToConnectRoom) {
                snackbarHostState.showSnackbar(message = context.getString(R.string.connection_failed))
            } else if (error is TogeError.NetworkError) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.connection_problem),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    LaunchedEffect(homeViewModel) {
        homeViewModel.sideEffect.collect { event ->
            when (event) {
                is HomeEffect.ReadyMoveToRoom -> {
                    CallNavDestination.navigateToCall(
                        navController = navController,
                        roomCode = event.roomCode,
                        isHost = event.isHost
                    )
                }
            }
        }
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            LoginState.Login -> {}
            LoginState.None -> {}
            LoginState.SessionExpired -> {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.session_expired),
                    duration = SnackbarDuration.Short
                )
            }

            LoginState.Logout -> {
                Log.e("HomeRouter", "Logged out")
            }
        }
    }

    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        modifier = modifier,
        /* STATE */
        state = uiState,
        isLoggedIn = loginState == LoginState.Login,
        /* EVENT */
        onEventUpdateEnterRoomCode = { newText ->
            homeViewModel.onEvent(HomeEvent.UpdateEnterRoomCode(newText))
        },
        onEventEnterRoom = { homeViewModel.onEvent(HomeEvent.EnterRoom) },
        onEventCreateRoom = { homeViewModel.onEvent(HomeEvent.CrateRoom) },
        /* NAVIGATION */
        onMoveToLogin = { navController.navigate(UserNavDestination.LOGIN) },
        onMoveToMyPage = { navController.navigate(UserNavDestination.MYPAGE) }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    /* STATE */
    state: HomeState = HomeState(),
    isLoggedIn: Boolean = false,
    /* EVENT */
    onEventUpdateEnterRoomCode: (String) -> Unit = {},
    onEventEnterRoom: () -> Unit = {},
    onEventCreateRoom: () -> Unit = {},
    /* NAVIGATION */
    onMoveToLogin: () -> Unit = {},
    onMoveToMyPage: () -> Unit = {},
) {
    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val cameraPermissionStr = Manifest.permission.CAMERA
    val micPermissionStr = Manifest.permission.RECORD_AUDIO
    val permissionHandler =
        rememberMultiPermissionHandler(listOf(cameraPermissionStr, micPermissionStr))
    val cameraPermissionData = permissionHandler.permissionsData[Manifest.permission.CAMERA]
    val micPermissionData = permissionHandler.permissionsData[Manifest.permission.RECORD_AUDIO]

    TogeScaffold(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus() // Use for clearFocus to HomeBottomBar.TextField
                })
            },
        contentPadding = PaddingValues(16.dp),
        topBar = {
            HomeTopBar(
                modifier = Modifier,
                title = stringResource(R.string.together),
                rightContent = {
                    if (isLoggedIn) {
                        ClickShrinkEffect(onClick = onMoveToMyPage) { CircularImage(imageUrl = state.profileUrl) }
                    } else {
                        TogeClickableText(
                            text = stringResource(R.string.login),
                            style = MaterialTheme.typography.bodyLarge,
                            onClick = { onMoveToLogin() }
                        )
                    }
                }
            )
        },
        bottomBar = {
            HomeBottomBar(
                isLogin = isLoggedIn,
                inputText = state.enterRoomCode,
                onInputTextChange = { newText ->
                    onEventUpdateEnterRoomCode(newText)
                },
                localFocusManager = localFocusManager,
                onMoveToCall = {
                    permissionHandler.requestAllPermissions()
                    onEventEnterRoom()
                }
            )
        },
        floatingActionButton = {
            if (isLoggedIn) {
                TogeFloatingButtonWithIcon(
                    enabled = true,
                    iconRes = R.drawable.ic_video_call,
                    text = stringResource(R.string.active_call),
                    onEnableClick = {
                        permissionHandler.requestAllPermissions(isMoveToSettings = false)
                        onEventCreateRoom()
                    },
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        isLoading = state.isLoading,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.permission_call_required),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            TogePermissionItem(
                icon = painterResource(id = R.drawable.ic_photo_camera),
                title = stringResource(R.string.camera),
                isGranted = cameraPermissionData?.status == PermissionHandlerStatus.GRANTED,
                onAllowClick = { permissionHandler.requestPermission(cameraPermissionStr) }
            )

            Spacer(modifier = Modifier.height(12.dp))

            TogePermissionItem(
                icon = painterResource(id = R.drawable.ic_mic),
                title = stringResource(R.string.microphone),
                isGranted = micPermissionData?.status == PermissionHandlerStatus.GRANTED,
                onAllowClick = { permissionHandler.requestPermission(micPermissionStr) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!permissionHandler.areAllPermissionsGranted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LocalTogeAppColor.current.grey900)
                        .padding(16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.permission_description_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalTogeAppColor.current.grey200,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_circle),
                            contentDescription = null,
                            modifier = Modifier.size(4.dp),
                            tint = LocalTogeAppColor.current.grey500,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.permission_description_camera),
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalTogeAppColor.current.grey200,
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_circle),
                            contentDescription = null,
                            modifier = Modifier.size(4.dp),
                            tint = LocalTogeAppColor.current.grey500,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.permission_description_microphone),
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalTogeAppColor.current.grey200,
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewHomeScreenLogin() {
    HomeScreen(
        state = HomeState(
            enterRoomCode = "1234",
            isLoading = false,
            profileUrl = ""
        ),
        isLoggedIn = true,
        onEventUpdateEnterRoomCode = {},
        onEventEnterRoom = {},
        onEventCreateRoom = {},
        onMoveToLogin = {},
        onMoveToMyPage = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenLogout() {
    HomeScreen(
        state = HomeState(
            enterRoomCode = "1234",
            isLoading = false,
            profileUrl = ""
        ),
        isLoggedIn = false,
        onEventUpdateEnterRoomCode = {},
        onEventEnterRoom = {},
        onEventCreateRoom = {},
        onMoveToLogin = {},
        onMoveToMyPage = {}
    )
}
