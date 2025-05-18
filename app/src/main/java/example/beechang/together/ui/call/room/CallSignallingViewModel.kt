package example.beechang.together.ui.call.room

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.usecase.room.ChangeCameraSuatusUseCase
import example.beechang.together.domain.usecase.room.ChangeMicStatusUseCase
import example.beechang.together.domain.usecase.room.GetRoomParticipantUseCase
import example.beechang.together.domain.usecase.room.ReceiveChangingCameraUseCase
import example.beechang.together.domain.usecase.room.ReceiveChangingMicUseCase
import example.beechang.together.domain.usecase.room.ReceiveUpdatingRoomParticipantUseCase
import example.beechang.together.domain.usecase.signalling.ReceiveAnswerUseCase
import example.beechang.together.domain.usecase.signalling.ReceiveIceCandidateUseCase
import example.beechang.together.domain.usecase.signalling.ReceiveOfferUseCase
import example.beechang.together.domain.usecase.signalling.ReceiveRtcReadyUseCase
import example.beechang.together.domain.usecase.signalling.SendAnswerUseCase
import example.beechang.together.domain.usecase.signalling.SendIceCandidateUseCase
import example.beechang.together.domain.usecase.signalling.SendOfferUseCase
import example.beechang.together.domain.usecase.signalling.SendRtcReadyUseCase
import example.beechang.together.domain.usecase.user.GetUserInfoUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import example.beechang.together.webrtc.PeerConnectionRole
import example.beechang.together.webrtc.SignallingEvent
import example.beechang.together.webrtc.TogeWebRtcManager
import example.beechang.together.webrtc.WebRtcAction
import example.beechang.together.webrtc.WebRtcAction.General.*
import example.beechang.together.webrtc.WebRtcData
import example.beechang.together.webrtc.di.TogeWebRtcManagerFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import org.webrtc.EglBase
import javax.inject.Inject


@HiltViewModel
class CallSignallingViewModel @Inject constructor(
    private val webRtcManagerFactory: TogeWebRtcManagerFactory,
    private val getRoomParticipantUseCase: GetRoomParticipantUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val receiveUpdatingRoomParticipantUseCase: ReceiveUpdatingRoomParticipantUseCase,
    private val sendRtcReadyUseCase: SendRtcReadyUseCase,
    private val receiveRtcReadyUseCase: ReceiveRtcReadyUseCase,
    private val sendOfferUseCase: SendOfferUseCase,
    private val receiveOfferUseCase: ReceiveOfferUseCase,
    private val sendAnswerUseCase: SendAnswerUseCase,
    private val receiveAnswerUseCase: ReceiveAnswerUseCase,
    private val sendIceCandidateUseCase: SendIceCandidateUseCase,
    private val receiveIceCandidateUseCase: ReceiveIceCandidateUseCase,
    private val changeMicStatusUseCase: ChangeMicStatusUseCase,
    private val changeCameraSuatusUseCase: ChangeCameraSuatusUseCase,
    private val receiveChangingMicUseCase: ReceiveChangingMicUseCase,
    private val receiveChangingCameraUseCase: ReceiveChangingCameraUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CallSignallingState, CallSignallingEvent, CallSignallingEffect>(
    savedStateHandle, CallSignallingState(), CALL_SIGNALLING_STATE
) {

    lateinit var webRtcManager: TogeWebRtcManager

    private val _webRtcState = MutableStateFlow(WebRtcState())
    val webRtcState: StateFlow<WebRtcState> = _webRtcState

    init {
        val roomCode = savedStateHandle.get<String>("roomCode") ?: ""
        val isHost = savedStateHandle.get<Boolean>("isHost") ?: false
        if (roomCode.isNotEmpty()) {
            updateState { copy(roomCode = roomCode) }
        }
        updateState { copy(isHost = isHost) }
    }

    override fun onEvent(event: CallSignallingEvent) {
        when (event) {
            CallSignallingEvent.GetRoomParticipantForSignaling -> {
                init()
            }

            is CallSignallingEvent.UpdatedRoomCode -> {
                updateState { copy(roomCode = event.roomCode) }
                init()
            }

            CallSignallingEvent.SwitchCamera -> {
                webRtcManager.processActionAsync(SwitchCamera(currentState.myUserId))
            }

            CallSignallingEvent.Disconnect -> {
                webRtcManager.release()
            }

            is CallSignallingEvent.UpdateEnabledHardware -> {
                updateState {
                    copy(
                        isEnabledCamera = event.isCameraOn,
                        isEnabledMic = event.isMicOn
                    )
                }
            }

            is CallSignallingEvent.ToggleAudioEnabled -> {
                toggleAudioEnabled(event.isEnabled)
            }

            is CallSignallingEvent.ToggleVideoEnabled -> {
                toggleVideoEnabled(event.isEnabled)
            }

            CallSignallingEvent.RefreshAudio -> {
                refreshAudioData()
            }

            CallSignallingEvent.RefreshVideo -> {
                refreshVideoData()
            }

            is CallSignallingEvent.ToggleSpeakerMute -> {
                 toggleSpeakerMute(event.isMuted)
            }
        }
    }

    private fun init() = viewModelScope.launch {
        prepareUserInfo()
        listeningRoomParticipantUpdated()
        listeningRtcReady()
        listeningOffer()
        listeningAnswer()
        listeningIceCandidate()
        liseningChangingMic()
        listeningChangingCamera()
    }

    private fun prepareUserInfo() = viewModelScope.launch {
        getUserInfoUseCase.getUserInfo().onSuccess { userInfo ->
            updateState { copy(myUserId = userInfo.userId) }
            getRoomParticipant {
                startWebRtc(userInfo.userId)
                listenParticipantsForWebrtcData()
                sendRtcReady()
                listeningSignallingEvent()
                updateDevicePermissionStatus()
            }
        }
    }

    private fun startWebRtc(userId: String = "") = viewModelScope.launch {
        webRtcManager = webRtcManagerFactory.create().apply {
            processActionAsync(InitWebRtc(if (userId.isNotEmpty()) userId else currentState.myUserId))

            if (currentState.isSpeakerMuted) {
                processActionAsync(
                    SetSpeakerMute(
                        userId = if (userId.isNotEmpty()) userId else currentState.myUserId,
                        isMuted = true
                    )
                )
            }
        }
        _webRtcState.update { it.copy(eglBase = webRtcManager.eglBase) }
    }

    private fun updateDevicePermissionStatus() = viewModelScope.launch {
        val roomCode = currentState.roomCode
        launch {
            changeMicStatusUseCase.invoke(
                roomCode = roomCode,
                isMicOn = currentState.isEnabledMic
            )
        }
        launch {
            changeCameraSuatusUseCase.invoke(
                roomCode = roomCode,
                isCameraOn = currentState.isEnabledCamera
            )
        }
        updateState { //서버에서 내 마이크,카메라 변경 알림이 오지않기때문에 클라에서 직접변경
            copy(participants = participants.updateParticipant(myUserId) {
                it.copy(isMicrophoneOn = isEnabledMic, isCameraOn = isEnabledCamera)
            })
        }
    }

    private fun toggleVideoEnabled(enabled: Boolean) = viewModelScope.launch {
        if (::webRtcManager.isInitialized) {
            webRtcManager.processActionAsync(
                ToggleVideo(userId = currentState.myUserId, enabled = enabled)
            )
        }
        changeCameraSuatusUseCase.invoke(roomCode = currentState.roomCode, isCameraOn = enabled)
        updateState {
            copy(
                isEnabledCamera = enabled,
                participants = participants.updateParticipant(myUserId) {
                    it.copy(isCameraOn = enabled)
                }
            )
        }
    }

    private fun toggleAudioEnabled(enabled: Boolean) = viewModelScope.launch {
        if (::webRtcManager.isInitialized) {
            webRtcManager.processActionAsync(
                ToggleAudio(userId = currentState.myUserId, enabled = enabled)
            )
        }
        changeMicStatusUseCase.invoke(roomCode = currentState.roomCode, isMicOn = enabled)
        updateState {
            copy(
                isEnabledMic = enabled,
                participants = participants.updateParticipant(myUserId) {
                    it.copy(isMicrophoneOn = enabled)
                }
            )
        }
    }

    private fun refreshVideoData() {
        if (::webRtcManager.isInitialized) {
            webRtcManager.processActionAsync(RefreshVideo(currentState.myUserId))
        }
    }

    private fun refreshAudioData() {
        if (::webRtcManager.isInitialized) {
            webRtcManager.processActionAsync(RefreshAudio(currentState.myUserId))
        }
    }

    private fun toggleSpeakerMute(isMuted: Boolean) = viewModelScope.launch {
        if (::webRtcManager.isInitialized) {
            webRtcManager.processActionAsync(
                SetSpeakerMute(userId = currentState.myUserId, isMuted = isMuted)
            )
        }

        updateState { copy(isSpeakerMuted = isMuted) }
    }

    private fun listenParticipantsForWebrtcData() = viewModelScope.launch {
        webRtcManager.participantMapFlow.collect { participantMap ->
            _webRtcState.update { it.copy(webRtcDataForParticipant = participantMap) }
        }
    }

    private fun getRoomParticipant(successCallBack: () -> Unit) = viewModelScope.launch {
        val roomCode = currentState.roomCode
        getRoomParticipantUseCase.invoke(roomCode = roomCode, isIncludingMySelf = true)
            .onSuccess { participants ->
                val currentMap = LinkedHashMap<String, RoomParticipantUi>(currentState.participants)
                participants.forEach { roomParticipant ->
                    currentMap[roomParticipant.userId] = roomParticipant.toUi()
                }
                updateState { copy(participants = currentMap) }
                successCallBack()
            }
    }

    private fun listeningRoomParticipantUpdated() = viewModelScope.launch {
        receiveUpdatingRoomParticipantUseCase.invoke()
            .collectInViewModel(
                onSuccess = { updatedRoomParticipant ->
                    val updatedUser = updatedRoomParticipant.updatedUser.toRoomParticipantUi()
                    val userId = updatedUser.userId
                    val isJoined = updatedRoomParticipant.isJoined
                    updateState {
                        val updatedParticipants = LinkedHashMap(participants)
                        if (isJoined) {
                            updatedParticipants[userId] = updatedUser
                        } else {
                            updatedParticipants.remove(userId)
                        }
                        copy(participants = updatedParticipants)
                    }

                    if (!isJoined) { //remove webrtc user
                        webRtcManager.processActionAsync(RemoveParticipant(userId))
                    }

                    sendEffect(
                        CallSignallingEffect.UpdatedCallRoomParticipant(
                            updatedUser = updatedRoomParticipant.updatedUser.toRoomParticipantUi(),
                            isJoined = updatedRoomParticipant.isJoined
                        )
                    )
                }
            )
    }

    private fun sendRtcReady() = viewModelScope.launch {
        val roomCode = currentState.roomCode
        currentState.participants.forEach {
            if (it.value.userId != currentState.myUserId) {
                webRtcManager.processAction(
                    CreatePeerConnection(
                        userId = it.value.userId,
                        role = PeerConnectionRole.Answerer
                    )
                )
            }
        }

        sendRtcReadyUseCase.invoke(roomCode = roomCode)
            .onError { _, _, _ ->
                sendError(TogeError.UnknownErrorFailedToCalling)
            }
    }

    private fun listeningRtcReady() = viewModelScope.launch {
        receiveRtcReadyUseCase.invoke()
            .collectInViewModel(
                onSuccess = { res ->
                    viewModelScope.launch {
                        webRtcManager.processActionAsync(
                            CreatePeerConnection(
                                userId = res.userId,
                                role = PeerConnectionRole.Offerer
                            )
                        )
                    }
                }
            )
    }

    private fun listeningOffer() = viewModelScope.launch {
        receiveOfferUseCase.invoke()
            .collectInViewModel(
                onSuccess = { res ->
                    webRtcManager.processActionAsync(
                        WebRtcAction.Signaling.SetOfferDescription(
                            userId = res.fromUserId,
                            sdp = res.sdp
                        )
                    )
                }
            )
    }

    private fun listeningAnswer() = viewModelScope.launch {
        receiveAnswerUseCase.invoke()
            .collectInViewModel(
                onSuccess = { res ->
                    webRtcManager.processActionAsync(
                        WebRtcAction.Signaling.SetAnswerDescription(
                            userId = res.fromUserId,
                            sdp = res.sdp
                        )
                    )
                }
            )
    }

    private fun listeningIceCandidate() = viewModelScope.launch {
        receiveIceCandidateUseCase.invoke()
            .collectInViewModel(
                onSuccess = { res ->
                    webRtcManager.processActionAsync(
                        WebRtcAction.Signaling.SetIceCandidate(
                            userId = res.fromUserId,
                            sdp = res.candidate,
                            sdpMid = res.sdpMid,
                            sdpMLineIndex = res.sdpMLineIndex
                        )
                    )
                }
            )
    }

    private fun listeningSignallingEvent() = viewModelScope.launch {
        webRtcManager.signallingEventFlow.collect { event ->
            when (event) {
                is SignallingEvent.SendOffer -> {
                    sendOfferUseCase.invoke(
                        roomCode = currentState.roomCode,
                        toUserId = event.toReceiver,
                        sdp = event.sdp
                    )
                }

                is SignallingEvent.SendAnswer -> {
                    sendAnswerUseCase.invoke(
                        roomCode = currentState.roomCode,
                        toUserId = event.toReceiver,
                        sdp = event.sdp
                    )
                }

                is SignallingEvent.SendIceCandidate -> {
                    sendIceCandidateUseCase.invoke(
                        roomCode = currentState.roomCode,
                        toUserId = event.toReceiver,
                        candidate = event.sdp,
                        sdpMLineIndex = event.sdpMLineIndex,
                        sdpMid = event.sdpMid
                    )
                }
            }
        }
    }

    private fun liseningChangingMic() = viewModelScope.launch {
        receiveChangingMicUseCase.invoke()
            .collectInViewModel(
                onSuccess = { res ->
                    updateState {
                        val updatedParticipants = LinkedHashMap(participants)
                        updatedParticipants[res.userId] = res.toUi()
                        copy(participants = updatedParticipants)
                    }
                }
            )
    }

    private fun listeningChangingCamera() = viewModelScope.launch {
        receiveChangingCameraUseCase.invoke()
            .collectInViewModel(
                onSuccess = { res ->
                    updateState {
                        val updatedParticipants = LinkedHashMap(participants)
                        updatedParticipants[res.userId] = res.toUi()
                        copy(participants = updatedParticipants)
                    }
                }
            )
    }


    private fun LinkedHashMap<String, RoomParticipantUi>.updateParticipant(
        userId: String,
        update: (RoomParticipantUi) -> RoomParticipantUi
    ): LinkedHashMap<String, RoomParticipantUi> {
        return LinkedHashMap(this).apply {
            get(userId)?.let { put(userId, update(it)) }
        }
    }

    companion object {
        const val CALL_SIGNALLING_STATE = "call_signalling_state"
    }
}

data class WebRtcState(
    val eglBase: EglBase? = null,
    val webRtcDataForParticipant: Map<String, WebRtcData> = emptyMap<String, WebRtcData>(),
)

@Parcelize
data class CallSignallingState(
    val roomCode: String = "",
    val isHost: Boolean = false,
    val myUserId: String = "",
    val isEnabledCamera: Boolean = true,
    val isEnabledMic: Boolean = true,
    val isSpeakerMuted: Boolean = false,
    val participants: LinkedHashMap<String, RoomParticipantUi> = linkedMapOf()
) : Parcelable, UiState

sealed interface CallSignallingEvent : UiEvent {
    data class UpdatedRoomCode(val roomCode: String) : CallSignallingEvent
    object GetRoomParticipantForSignaling : CallSignallingEvent
    object SwitchCamera : CallSignallingEvent
    object Disconnect : CallSignallingEvent
    data class UpdateEnabledHardware(val isCameraOn: Boolean, val isMicOn: Boolean) :
        CallSignallingEvent

    data class ToggleVideoEnabled(val isEnabled: Boolean) : CallSignallingEvent
    data class ToggleAudioEnabled(val isEnabled: Boolean) : CallSignallingEvent
    object RefreshVideo : CallSignallingEvent
    object RefreshAudio : CallSignallingEvent
    data class ToggleSpeakerMute(val isMuted: Boolean) : CallSignallingEvent
}

sealed interface CallSignallingEffect : UiEffect {
    data class UpdatedCallRoomParticipant(
        val updatedUser: RoomParticipantUi,
        val isJoined: Boolean
    ) : CallSignallingEffect
}