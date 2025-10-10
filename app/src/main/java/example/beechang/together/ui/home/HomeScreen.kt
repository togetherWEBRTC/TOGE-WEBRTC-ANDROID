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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.model.InquiryCategory
import example.beechang.together.domain.model.LoginState
import example.beechang.together.ui.call.CallNavDestination
import example.beechang.together.ui.component.bottombar.HomeBottomBar
import example.beechang.together.ui.component.bottomsheet.InquiryBottomSheet
import example.beechang.together.ui.component.button.TogeFloatingButtonWithIcon
import example.beechang.together.ui.component.card.TogePermissionItem
import example.beechang.together.ui.component.dialog.TogeDialog
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.text.TogeClickableText
import example.beechang.together.ui.component.topbar.HomeTopBar
import example.beechang.together.ui.component.util.CircularImage
import example.beechang.together.ui.component.util.ClickShrinkEffect
import example.beechang.together.ui.component.util.shimmer
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

                HomeEffect.InquirySuccess -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.inquiry_success_message),
                        duration = SnackbarDuration.Short
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
        isShowSkeletonUi = uiState.isShowSkeleton,
        snackbarHostState = snackbarHostState,
        /* EVENT */
        onEventUpdateEnterRoomCode = { newText ->
            homeViewModel.onEvent(HomeEvent.UpdateEnterRoomCode(newText))
        },
        onEventEnterRoom = { homeViewModel.onEvent(HomeEvent.EnterRoom) },
        onEventCreateRoom = { homeViewModel.onEvent(HomeEvent.CrateRoom) },
        onEventCreateInquiry = { category, content ->
            homeViewModel.onEvent(HomeEvent.CreateInquiry(category, content))
        },
        onEventDismissDuplicateConnectionDialog = {
            homeViewModel.onEvent(HomeEvent.DismissDuplicateConnectionDialog)
        },
        onEventHandleDuplicateConnectionChoice = { forceDisconnectExisting ->
            homeViewModel.onEvent(HomeEvent.HandleDuplicateConnectionChoice(forceDisconnectExisting))
        },
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
    isShowSkeletonUi: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* EVENT */
    onEventUpdateEnterRoomCode: (String) -> Unit = {},
    onEventEnterRoom: () -> Unit = {},
    onEventCreateRoom: () -> Unit = {},
    onEventCreateInquiry: (InquiryCategory, String/*content*/) -> Unit = { _, _ -> },
    onEventDismissDuplicateConnectionDialog: () -> Unit = {},
    onEventHandleDuplicateConnectionChoice: (Boolean/*forceDisconnectExisting*/) -> Unit = {},
    /* NAVIGATION */
    onMoveToLogin: () -> Unit = {},
    onMoveToMyPage: () -> Unit = {},
) {
    val context = LocalContext.current
    val localFocusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    var showInquiryBottomSheet by remember { mutableStateOf(false) }

    val cameraPermissionStr = Manifest.permission.CAMERA
    val micPermissionStr = Manifest.permission.RECORD_AUDIO
    val notificationPermissionStr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else null

    val permissions = mutableListOf(cameraPermissionStr, micPermissionStr)
    notificationPermissionStr?.let { permissions.add(it) }

    val permissionHandler = rememberMultiPermissionHandler(permissions)
    val cameraPermissionData = permissionHandler.permissionsData[Manifest.permission.CAMERA]
    val micPermissionData = permissionHandler.permissionsData[Manifest.permission.RECORD_AUDIO]
    val notificationPermissionData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        permissionHandler.permissionsData[Manifest.permission.POST_NOTIFICATIONS]
    } else null

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionStr?.let { permission ->
                val permissionData = permissionHandler.permissionsData[permission]
                if (permissionData?.status == PermissionHandlerStatus.SHOULD_REQUEST) {
                    permissionHandler.requestPermission(permission, isMoveToSettings = false)
                }
            }
        }
    }

    TogeDialog(
        isShowDialog = state.showDuplicateConnectionDialog,
        title = stringResource(R.string.duplicate_connection_title),
        content = stringResource(R.string.duplicate_connection_message),
        confirmButtonText = stringResource(R.string.disconnect_existing_connection),
        dismissButtonText = stringResource(R.string.cancel),
        onConfirm = {
            onEventHandleDuplicateConnectionChoice(true)
        },
        onDismiss = {
            onEventDismissDuplicateConnectionDialog()
        },
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )

    InquiryBottomSheet(
        isShow = showInquiryBottomSheet,
        onDismissRequest = {
            showInquiryBottomSheet = false
        },
        onConfirmInquiry = { category, content ->
            onEventCreateInquiry(category, content)
            showInquiryBottomSheet = false
        }
    )

    TogeScaffold(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus() // Use for clearFocus to HomeBottomBar.TextField
                })
            },
        contentPadding = PaddingValues(16.dp),
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        topBar = {
            HomeTopBar(
                modifier = Modifier,
                title = stringResource(R.string.together),
                rightContent = {
                    if (!isShowSkeletonUi) {
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
                }
            )
        },
        bottomBar = {
            if (isShowSkeletonUi) {
                HomeBottomShimmer()
            } else {
                HomeBottomBar(
                    modifier = Modifier,
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
            }
        },
        floatingActionButton = {
            if (isLoggedIn && !isShowSkeletonUi) {
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
            InquiryFeedbackCard(onClick = { showInquiryBottomSheet = true })

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.permission_call_required),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isShowSkeletonUi) {
                HomeContentShimmer()
                return@Column
            }

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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(modifier = Modifier.height(12.dp))

                TogePermissionItem(
                    icon = painterResource(id = R.drawable.ic_notification_active),
                    title = stringResource(R.string.notify),
                    isGranted = notificationPermissionData?.status == PermissionHandlerStatus.GRANTED,
                    onAllowClick = {
                        notificationPermissionStr?.let { permissionHandler.requestPermission(it) }
                    }
                )
            }

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

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_notification_active),
                                contentDescription = null,
                                modifier = Modifier.size(4.dp),
                                tint = LocalTogeAppColor.current.grey500,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.permission_description_notification),
                                style = MaterialTheme.typography.bodySmall,
                                color = LocalTogeAppColor.current.grey200,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InquiryFeedbackCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ClickShrinkEffect(
        shrinkFactor = 0.9f,
        onClick = onClick
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(bottom = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.inquiry_feedback_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_touch),
                    contentDescription = "touch",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun HomeBottomShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .shimmer(radius = 12.dp)
    )
}

@Composable
fun HomeContentShimmer() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val repeatCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            3
        } else {
            2
        }

        repeat(repeatCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .shimmer(radius = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
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

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreenLoading() {
    HomeScreen(
        state = HomeState(
            enterRoomCode = "1234",
            isLoading = true,
            profileUrl = "",
            isShowSkeleton = true
        ),
        isShowSkeletonUi = true,
    )
}
