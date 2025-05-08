package example.beechang.together.ui.call.room

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.ui.call.room.CallRoomEvent.*
import example.beechang.together.ui.call.room.CallSignallingEvent.*
import example.beechang.together.ui.component.util.webrtc.VideoCallLayout
import example.beechang.together.ui.component.bottombar.CallingBottomBar
import example.beechang.together.ui.component.dialog.TogeDialog
import example.beechang.together.ui.component.dialog.TogeOnlyConfirmBtnDialog
import example.beechang.together.ui.component.scaffold.AnimatedTogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.topbar.CallingTopBar
import example.beechang.together.ui.utils.PermissionHandlerStatus
import example.beechang.together.ui.utils.rememberMultiPermissionHandler
import kotlinx.coroutines.CoroutineScope

@Composable
fun CallRoomRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {

    val roomViewModel: CallRoomViewModel = hiltViewModel()
    val signallingViewModel: CallSignallingViewModel = hiltViewModel()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionHandler =
        rememberMultiPermissionHandler(
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    val cameraPermissionData = permissionHandler.permissionsData[Manifest.permission.CAMERA]
    val micPermissionData = permissionHandler.permissionsData[Manifest.permission.RECORD_AUDIO]

    LaunchedEffect(Unit) {
        signallingViewModel.onEvent(
            UpdateEnabledHardware(
                isCameraOn = cameraPermissionData?.status == PermissionHandlerStatus.GRANTED,
                isMicOn = micPermissionData?.status == PermissionHandlerStatus.GRANTED
            )
        )
        signallingViewModel.onEvent(ToggleSpeakerMute(false))

        val isHost = navBackStackEntry.arguments?.getBoolean("isHost") ?: false
        if (isHost) { // 방생성
            roomViewModel.onEvent(
                CreateCallRoom(
                    roomCode = navBackStackEntry.arguments?.getString("roomCode") ?: ""
                )
            )
        } else { // 통화연결
            signallingViewModel.onEvent(GetRoomParticipantForSignaling)
        }
    }

    val permissionInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(cameraPermissionData?.status, micPermissionData?.status) {
        if (permissionInitialized.value) {
            if (cameraPermissionData?.status == PermissionHandlerStatus.GRANTED) {
                signallingViewModel.onEvent(ToggleVideoEnabled(true))
                signallingViewModel.onEvent(RefreshVideo)
            }

            if (micPermissionData?.status == PermissionHandlerStatus.GRANTED) {
                signallingViewModel.onEvent(ToggleAudioEnabled(true))
                signallingViewModel.onEvent(RefreshAudio)
            }
        } else {
            permissionInitialized.value = true
        }
    }

    val handleToggleCamera: (Boolean) -> Unit = { enabled ->
        if (enabled && cameraPermissionData?.status != PermissionHandlerStatus.GRANTED) {
            permissionHandler.requestPermission(
                permission = Manifest.permission.CAMERA,
                isMoveToSettings = false
            ) {

            }
        } else {
            signallingViewModel.onEvent(ToggleVideoEnabled(enabled))
        }
    }

    val handleToggleMic: (Boolean) -> Unit = { enabled ->
        if (enabled && micPermissionData?.status != PermissionHandlerStatus.GRANTED) {
            permissionHandler.requestPermission(Manifest.permission.RECORD_AUDIO)
        } else {
            signallingViewModel.onEvent(ToggleAudioEnabled(enabled))
        }
    }

    LaunchedEffect(roomViewModel) {
        roomViewModel.sideEffect.collect { effect ->
            when (effect) {
                is CallRoomEffect.SuccessCreateCallRoom -> {
                    signallingViewModel.onEvent(
                        CallSignallingEvent.UpdatedRoomCode(effect.roomCode)
                    )
                }

                is CallRoomEffect.NotifyNewWaitingMember -> { // 웨이팅 참여자 추가알림, 승인 시 승인요청 진행
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.user_wants_to_join_call, effect.updatedUser.name
                        ),
                        actionLabel = context.getString(R.string.approve),
                        withDismissAction = true,
                    ).apply {
                        when (this) {
                            SnackbarResult.Dismissed -> {}
                            SnackbarResult.ActionPerformed -> {
                                roomViewModel.onEvent(
                                    CallRoomEvent.DecideWaitingApproval(
                                        userId = effect.updatedUser.userId, isApprove = true
                                    )
                                )
                            }
                        }
                    }
                }

                CallRoomEffect.SuccessDisconnectRoom -> {
                    navController.popBackStack()
                }
            }
        }
    }

    LaunchedEffect(roomViewModel) {
        roomViewModel.errorEffect.collect { error ->
            if (error is TogeError.FailedToCreateRoom) {
                //todo show error dialog -> out of room
            } else if (error is TogeError.RoomNotFound) {

            }
        }
    }

    LaunchedEffect(signallingViewModel) {
        signallingViewModel.webRtcState.collect {

        }
    }

    BackHandler { roomViewModel.onEvent(SwitchShowDisconnectRoomDialog) }

    val roomState by roomViewModel.uiState.collectAsStateWithLifecycle()
    val signallingState by signallingViewModel.uiState.collectAsStateWithLifecycle()
    val wrtcState by signallingViewModel.webRtcState.collectAsStateWithLifecycle()

    CallRoomScreen(
        modifier = modifier, snackbarHostState = snackbarHostState,
        /* STATE */
        roomState = roomState, signallingState = signallingState, wrtcState = wrtcState,
        isCameraOn = cameraPermissionData?.status == PermissionHandlerStatus.GRANTED && signallingState.participants[signallingState.myUserId]?.isCameraOn == true,
        isMicOn = micPermissionData?.status == PermissionHandlerStatus.GRANTED && signallingState.participants[signallingState.myUserId]?.isMicrophoneOn == true,
        isSpeakerMuted = signallingState.isSpeakerMuted,
        /* EVENT */
        onEventDisconnect = {
            roomViewModel.onEvent(WebSocketDisconnect)
            signallingViewModel.onEvent(Disconnect)
        },
        onEventSwitchShowDisconnectRoomDialog = { roomViewModel.onEvent(
            SwitchShowDisconnectRoomDialog
        ) },
        onEventSwitchShowPermissionToSettingDialog = { roomViewModel.onEvent(
            SwitchShowDialogPermission
        ) },
        onEventSwitchCamera = { signallingViewModel.onEvent(SwitchCamera) },
        onEventToggleOnOffCamera = handleToggleCamera,
        onEventToggleOnOffMic = handleToggleMic,
        onEventToggleSpeakerMute = { isMuted ->
            signallingViewModel.onEvent(ToggleSpeakerMute(isMuted))
        },
    )
}

@Composable
fun CallRoomScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* STATE */
    roomState: CallRoomState,
    signallingState: CallSignallingState,
    wrtcState: WebRtcState,
    isCameraOn: Boolean = true,
    isMicOn: Boolean = true,
    isSpeakerMuted: Boolean = false,
    /* EVENT */
    onEventDisconnect: () -> Unit = {},
    onEventSwitchShowDisconnectRoomDialog: () -> Unit = {},
    onEventSwitchShowPermissionToSettingDialog: () -> Unit = {},
    onEventSwitchCamera: () -> Unit = { },
    onEventToggleOnOffCamera: (Boolean) -> Unit = { },
    onEventToggleOnOffMic: (Boolean) -> Unit = { },
    onEventToggleSpeakerMute: (Boolean) -> Unit = { },
) {

    val layoutType = when (signallingState.participants.size) {
        0, 1 -> CallLayoutType.SINGLE
        2 -> CallLayoutType.FLOATING
        else -> CallLayoutType.GRID
    }

    /* DIALOG */
    TogeOnlyConfirmBtnDialog(
        isShowDialog = roomState.isShowDialogForWrongRoomCode,
        title = stringResource(R.string.error),
        content = stringResource(R.string.connection_failed),
        onConfirm = {
            onEventDisconnect()
        }
    )

    TogeDialog(
        isShowDialog = roomState.isShowDialogDisconnectRoom,
        title = stringResource(R.string.ok),
        content = stringResource(R.string.confirm_end_call),
        onConfirm = {
            onEventDisconnect()
            onEventSwitchShowDisconnectRoomDialog()
        },
        onDismiss = {
            onEventSwitchShowDisconnectRoomDialog()
        },
    )

    TogeDialog(
        isShowDialog = roomState.isShowDialogPermission,
        title = stringResource(R.string.error),
        content = stringResource(R.string.permission_settings_required),
        onConfirm = {
            onEventSwitchShowPermissionToSettingDialog()
        },
        onDismiss = {
            onEventSwitchShowPermissionToSettingDialog()
        },
    )

    /* UI */
    AnimatedTogeScaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = roomState.isLoading,
        autoHideDelay = 10000L,
        topBar = {
            CallingTopBar(
                isVolumeOn = !isSpeakerMuted,
                onClickCallEnd = { onEventSwitchShowDisconnectRoomDialog() },
                onClickToggleSpeaker = { onEventToggleSpeakerMute(!isSpeakerMuted) },
                onClickSwitchCamera = { onEventSwitchCamera() },
            )
        },
        bottomBar = {
            CallingBottomBar(
                isCameraOn = isCameraOn,
                isMicOn = isMicOn,
                onClickCamera = { onEventToggleOnOffCamera(!isCameraOn) },
                onClickMic = { onEventToggleOnOffMic(!isMicOn) },
                onClickParticipant = { },
                onClickChat = { }
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            color = Color.Transparent,
        ) {
            if (signallingState.participants.isNotEmpty()) {
                VideoCallLayout(
                    modifier = Modifier,
                    myUserId = signallingState.myUserId,
                    participants = signallingState.participants,
                    webRtcData = wrtcState.webRtcDataForParticipant,
                    layoutType = layoutType,
                    eglBase = wrtcState.eglBase,
                    onParticipantSwap = { userId1, userId2 -> },
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewCallRoomScreen() {
    CallRoomScreen(
        roomState = CallRoomState(),
        signallingState = CallSignallingState(),
        wrtcState = WebRtcState(),
        onEventDisconnect = {},
        onEventSwitchShowDisconnectRoomDialog = {},
        onEventSwitchCamera = {}
    )
}
