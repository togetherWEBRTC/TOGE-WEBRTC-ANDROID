package example.beechang.together.ui.call.room

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import example.beechang.together.ui.component.bottomsheet.ParticipantBottomSheet
import example.beechang.together.ui.component.dialog.TogeDialog
import example.beechang.together.ui.component.dialog.TogeOnlyConfirmBtnDialog
import example.beechang.together.ui.component.scaffold.AnimatedTogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.topbar.CallingTopBar
import example.beechang.together.ui.utils.LocalWebRtcServiceManager
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
    val webRtcServiceManager = LocalWebRtcServiceManager.current
    var participantInfoToExpel by remember { mutableStateOf<RoomParticipantUi?>(null) }
    /* BottomSheetState */
    val participantBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isShowParticipantBottomSheet by remember { mutableStateOf(false) }

    /* DialogStates */
    var isShowDialogForWrongRoomCode by remember { mutableStateOf(false) }
    var isShowDialogDisconnectRoom by remember { mutableStateOf(false) }
    var isShowDialogPermission by remember { mutableStateOf(false) }
    var isShowDialogConnectionDisconnected by remember { mutableStateOf(false) }
    var isShowDialogExpelParticipant by remember { mutableStateOf(false) }
    var isShowDialogBeExpelled by remember { mutableStateOf(false) }

    val permissionHandler =
        rememberMultiPermissionHandler(
            listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    val cameraPermissionData = permissionHandler.permissionsData[Manifest.permission.CAMERA]
    val micPermissionData = permissionHandler.permissionsData[Manifest.permission.RECORD_AUDIO]

    val hasInitialized = rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (hasInitialized.value) {
            return@LaunchedEffect
        } else {
            hasInitialized.value = true
        }

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

    // 쿼리파라미터로 넘어온 액션 처리 - stop 통화중지
    LaunchedEffect(navBackStackEntry.savedStateHandle) {
        navBackStackEntry.savedStateHandle.getStateFlow<String?>("action", null).collect { action ->
            if (action == "stop") {
                isShowDialogDisconnectRoom = true
                navBackStackEntry.savedStateHandle["action"] = null
            }
        }
    }

    // 퍼미션 변경 시
    val permissionInitialized = remember { mutableStateOf(false) }
    LaunchedEffect(cameraPermissionData?.status, micPermissionData?.status) {
        if (permissionInitialized.value) {
            var needServiceRestart = false

            if (cameraPermissionData?.status == PermissionHandlerStatus.GRANTED) {
                signallingViewModel.onEvent(ToggleVideoEnabled(true))
                signallingViewModel.onEvent(RefreshVideo)
                needServiceRestart = true
            }

            if (micPermissionData?.status == PermissionHandlerStatus.GRANTED) {
                signallingViewModel.onEvent(ToggleAudioEnabled(true))
                signallingViewModel.onEvent(RefreshAudio)
                needServiceRestart = true
            }

            if (needServiceRestart) {
                webRtcServiceManager.restartCall()
            }
        } else {
            permissionInitialized.value = true
        }
    }

    val handleToggleCamera: (Boolean) -> Unit = { enabled ->
        if (enabled && cameraPermissionData?.status != PermissionHandlerStatus.GRANTED) {
            permissionHandler.requestPermission(
                permission = Manifest.permission.CAMERA
            )
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
                        UpdatedRoomCode(effect.roomCode)
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
                                    DecideWaitingApproval(
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

                CallRoomEffect.SuccessExpelMember -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.success_excluded_member)
                    )
                }

                is CallRoomEffect.BeExpelledRoom -> {
                    webRtcServiceManager.release()
//                    roomViewModel.WebSocketDisconnect 시 알림을 별도로 받고 자동으로 뒤로가기 처리 -> 얼럿을 못 볼 수 있으니 얼럿 누르는 시점에서 별도로 처리
//                    roomViewModel.onEvent(CallRoomEvent.WebSocketDisconnect)
                    signallingViewModel.onEvent(CallSignallingEvent.Disconnect)
                    isShowDialogBeExpelled = true
                }
            }
        }
    }

    LaunchedEffect(roomViewModel) {
        roomViewModel.errorEffect.collect { error ->
            if (error is TogeError.FailedToCreateRoom || error is TogeError.FailedToConnectRoom) {
                isShowDialogConnectionDisconnected = true
            } else if (error is TogeError.RoomNotFound) {

            }
        }
    }

    LaunchedEffect(signallingViewModel) {
        signallingViewModel.webRtcState.collect {

        }
    }

    BackHandler { isShowDialogDisconnectRoom = true }

    val roomState by roomViewModel.uiState.collectAsStateWithLifecycle()
    val signallingState by signallingViewModel.uiState.collectAsStateWithLifecycle()
    val wrtcState by signallingViewModel.webRtcState.collectAsStateWithLifecycle()

    CallRoomScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        participantBottomSheetState = participantBottomSheetState,
        /* STATE */
        roomState = roomState, signallingState = signallingState, wrtcState = wrtcState,
        isCameraOn = cameraPermissionData?.status == PermissionHandlerStatus.GRANTED && signallingState.participants[signallingState.myUserId]?.isCameraOn == true,
        isMicOn = micPermissionData?.status == PermissionHandlerStatus.GRANTED && signallingState.participants[signallingState.myUserId]?.isMicrophoneOn == true,
        participantInfoToExpel = participantInfoToExpel,
        isSpeakerMuted = signallingState.isSpeakerMuted,
        isShowDialogExpelParticipant = isShowDialogExpelParticipant,
        isShowParticipantBottomSheet = isShowParticipantBottomSheet,
        isShowDialogForWrongRoomCode = isShowDialogForWrongRoomCode,
        isShowDialogDisconnectRoom = isShowDialogDisconnectRoom,
        isShowDialogConnectionDisconnected = isShowDialogConnectionDisconnected,
        isShowDialogPermission = isShowDialogPermission,
        isShowDialogBeExpelled = isShowDialogBeExpelled,
        /* EVENT */
        onEventDisconnect = {
            webRtcServiceManager.release()
            roomViewModel.onEvent(CallRoomEvent.WebSocketDisconnect)
            signallingViewModel.onEvent(CallSignallingEvent.Disconnect)
        },
        onEventDismissDisconnectDialog = { isShowDialogDisconnectRoom = false },
        onEventShowDisconnectDialog = { isShowDialogDisconnectRoom = true },
        onEventDismissPermissionDialog = { isShowDialogPermission = false },
        onEventDismissWrongRoomCodeDialog = { isShowDialogForWrongRoomCode = false },
        onEventDismissConnectionDisconnected = { isShowDialogConnectionDisconnected = false },
        onEventSwitchCamera = { signallingViewModel.onEvent(SwitchCamera) },
        onEventToggleOnOffCamera = handleToggleCamera,
        onEventToggleOnOffMic = handleToggleMic,
        onEventToggleSpeakerMute = { isMuted ->
            signallingViewModel.onEvent(ToggleSpeakerMute(isMuted))
        },
        onEventUpdateParticipantBottomSheetState = { bool -> isShowParticipantBottomSheet = bool },
        onEventDecideWaiting = { userId, isApprove ->
            roomViewModel.onEvent(
                CallRoomEvent.DecideWaitingApproval(userId = userId, isApprove = isApprove)
            )
            snackbarHostState.currentSnackbarData?.dismiss()
        },
        onEventShowExpelDialog = { userId ->
            participantInfoToExpel = signallingState.participants[userId]
            isShowDialogExpelParticipant = true
        },
        onEventDismissExpelDialog = {
            isShowDialogExpelParticipant = false
            participantInfoToExpel = null
        },
        onEventExpelParticipant = { userId ->
            roomViewModel.onEvent(CallRoomEvent.ExpelMember(userId))
            isShowDialogExpelParticipant = false
            participantInfoToExpel = null
        },
        onEventDismissBeExpelledDialog = {
            isShowDialogBeExpelled = false
            roomViewModel.onEvent(CallRoomEvent.WebSocketDisconnect)
        }
    )
}

// ------------------------ SCREEN ------------------------
@Composable
fun CallRoomScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    participantBottomSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    /* STATE */
    roomState: CallRoomState,
    signallingState: CallSignallingState,
    wrtcState: WebRtcState,
    isCameraOn: Boolean = true,
    isMicOn: Boolean = true,
    participantInfoToExpel: RoomParticipantUi? = null,
    isSpeakerMuted: Boolean = false,
    isShowDialogExpelParticipant: Boolean = false,
    isShowParticipantBottomSheet: Boolean = false,
    isShowDialogForWrongRoomCode: Boolean = false,
    isShowDialogConnectionDisconnected: Boolean = false,
    isShowDialogDisconnectRoom: Boolean = false,
    isShowDialogPermission: Boolean = false,
    isShowDialogBeExpelled: Boolean = false,
    /* EVENT */
    onEventDisconnect: () -> Unit = {},
    onEventDismissDisconnectDialog: () -> Unit = {},
    onEventShowDisconnectDialog: () -> Unit = {},
    onEventDismissPermissionDialog: () -> Unit = {},
    onEventDismissWrongRoomCodeDialog: () -> Unit = {},
    onEventDismissConnectionDisconnected: () -> Unit = {},
    onEventSwitchCamera: () -> Unit = { },
    onEventToggleOnOffCamera: (Boolean) -> Unit = { },
    onEventToggleOnOffMic: (Boolean) -> Unit = { },
    onEventToggleSpeakerMute: (Boolean) -> Unit = { },
    onEventUpdateParticipantBottomSheetState: (Boolean) -> Unit = { },
    onEventDecideWaiting: (String/*userId*/, Boolean/*isApprove*/) -> Unit = { _, _ -> },
    onEventShowExpelDialog: (String/*userId*/) -> Unit = {},
    onEventDismissExpelDialog: () -> Unit = {},
    onEventExpelParticipant: (String/*targetUserId*/) -> Unit = {},
    onEventDismissBeExpelledDialog: () -> Unit = {},
) {

    val layoutType = when (signallingState.participants.size) {
        0, 1 -> CallLayoutType.SINGLE
        2 -> CallLayoutType.FLOATING
        else -> CallLayoutType.GRID
    }

    /* DIALOG */
    TogeOnlyConfirmBtnDialog(
        isShowDialog = isShowDialogConnectionDisconnected,
        title = stringResource(R.string.end_call),
        content = stringResource(R.string.connection_problem),
        onConfirm = {
            onEventDisconnect()
            onEventDismissConnectionDisconnected()
        }
    )

    TogeOnlyConfirmBtnDialog(
        isShowDialog = isShowDialogForWrongRoomCode,
        title = stringResource(R.string.error),
        content = stringResource(R.string.connection_failed),
        onConfirm = {
            onEventDisconnect()
            onEventDismissWrongRoomCodeDialog()
        }
    )

    TogeOnlyConfirmBtnDialog(
        isShowDialog = isShowDialogBeExpelled,
        title = stringResource(R.string.action_exclude_participant),
        content = stringResource(R.string.notification_excluded_by_host),
        onConfirm = { onEventDismissBeExpelledDialog() }
    )

    TogeDialog(
        isShowDialog = isShowDialogDisconnectRoom,
        title = stringResource(R.string.ok),
        content = stringResource(R.string.confirm_end_call),
        onConfirm = {
            onEventDisconnect()
            onEventDismissDisconnectDialog()
        },
        onDismiss = {
            onEventDismissDisconnectDialog()
        }
    )

    TogeDialog(
        isShowDialog = isShowDialogPermission,
        title = stringResource(R.string.error),
        content = stringResource(R.string.permission_settings_required),
        onConfirm = {
            onEventDismissPermissionDialog()
        },
        onDismiss = {
            onEventDismissPermissionDialog()
        }
    )

    TogeDialog(
        isShowDialog = isShowDialogExpelParticipant,
        title = stringResource(R.string.exclude_participant_title),
        content = stringResource(
            id = R.string.confirm_exclude_participant_message,
            participantInfoToExpel?.name ?: stringResource(R.string.participant_title)
        ),
        onConfirm = {
            participantInfoToExpel?.userId?.let { onEventExpelParticipant(it) }
        },
        onDismiss = { onEventDismissExpelDialog() }
    )

    /* Bottom Sheet */
    ParticipantBottomSheet(
        modifier = modifier,
        modalSheetState = participantBottomSheetState,
        isShow = isShowParticipantBottomSheet,
        isHost = roomState.isHost,
        myUserId = signallingState.myUserId,
        roomCode = roomState.roomCode,
        waitingParticipants = roomState.waitingParticipants,
        participants = signallingState.toParticipantList(),
        onDismissRequest = { onEventUpdateParticipantBottomSheetState(false) },
        onApproveWaiting = { userId -> onEventDecideWaiting(userId, true) },
        onRejectWaiting = { userId -> onEventDecideWaiting(userId, false) },
        onExpelParticipant = { userId -> onEventShowExpelDialog(userId) }
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
                onClickCallEnd = { onEventShowDisconnectDialog() },
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
                onClickParticipant = { onEventUpdateParticipantBottomSheetState(true)/* ParticipantBottomSheet */ },
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
        onEventDismissDisconnectDialog = {},
        onEventShowDisconnectDialog = {},
        onEventDismissPermissionDialog = {},
        onEventDismissWrongRoomCodeDialog = {},
        onEventSwitchCamera = {}
    )
}
