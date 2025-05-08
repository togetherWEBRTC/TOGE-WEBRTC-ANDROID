package example.beechang.together.ui.call.room

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.usecase.room.CreateRoomUseCase
import example.beechang.together.domain.usecase.room.DecideWaitingEnterFromHostUseCase
import example.beechang.together.domain.usecase.room.DisconnectRoomUseCase
import example.beechang.together.domain.usecase.room.ReceiveWaitingNotifyUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class CallRoomViewModel @Inject constructor(
    private val createRoomUseCase: CreateRoomUseCase,
    private val disconnectRoomUseCase: DisconnectRoomUseCase,
    private val decideWaitingMemberEnterRoomUseCase: DecideWaitingEnterFromHostUseCase,
    private val receiveWaitingNotifyUseCase: ReceiveWaitingNotifyUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CallRoomState, CallRoomEvent, CallRoomEffect>(
    savedStateHandle, CallRoomState(), CALL_ROOM_STATE
) {
    init {
        val isHost = savedStateHandle.get<Boolean>("isHost") ?: false
        if (savedStateHandle.get<String>("roomCode").isNullOrEmpty()) {
            updateState { copy(isHost = isHost) }
        } else {
            updateState { copy(roomCode = roomCode, isHost = isHost) }
        }

        listeningWaitingNotify()
    }

    override fun onEvent(event: CallRoomEvent) {
        when (event) {
            CallRoomEvent.WebSocketDisconnect -> {
                disconnect()
            }

            is CallRoomEvent.CreateCallRoom -> {
                createRoom(event.roomCode)
            }

            is CallRoomEvent.DecideWaitingApproval -> {
                decideWaitingApproval(
                    roomCode = currentState.roomCode,
                    userId = event.userId,
                    isApprove = event.isApprove
                )
            }

            CallRoomEvent.SwitchShowDialogForWrongRoomCode -> {
                updateState { copy(isShowDialogForWrongRoomCode = !currentState.isShowDialogForWrongRoomCode) }
            }

            CallRoomEvent.SwitchShowDisconnectRoomDialog -> {
                updateState { copy(isShowDialogDisconnectRoom = !currentState.isShowDialogDisconnectRoom) }
            }

            CallRoomEvent.SwitchShowDialogPermission -> {
                updateState { copy(isShowDialogPermission = !currentState.isShowDialogPermission) }
            }

        }
    }

    private fun createRoom(roomCode: String) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { createRoomUseCase.invoke(roomCode) },
            onSuccess = { result ->
                if (result.roomCode.isNotEmpty()) {
                    sendEffect(CallRoomEffect.SuccessCreateCallRoom(roomCode = result.roomCode))
                    updateState { copy(roomCode = result.roomCode) }
                } else {
                    sendError(TogeError.FailedToCreateRoom)
                }
            },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun disconnect() = viewModelScope.launch {
        disconnectRoomUseCase.invoke().onFinally {
            sendEffect(CallRoomEffect.SuccessDisconnectRoom)
        }
    }

    private fun decideWaitingApproval(roomCode: String, userId: String, isApprove: Boolean) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { decideWaitingMemberEnterRoomUseCase(roomCode, userId, isApprove) },
            onSuccess = {},
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun listeningWaitingNotify() = viewModelScope.launch {
        receiveWaitingNotifyUseCase.invoke()
            .collectInViewModel(
                onSuccess = { roomWaitingMembers ->
                    val waitingList =
                        roomWaitingMembers.waitingList.map { it.toRoomParticipantUi() }
                    if (roomWaitingMembers.isAdded) {
                        val updatedUser = roomWaitingMembers.updatedUser.toRoomParticipantUi()
                        sendEffect(
                            CallRoomEffect.NotifyNewWaitingMember(
                                roomCode = currentState.roomCode,
                                updatedUser
                            )
                        )
                    }
                    updateState { copy(waitingParticipants = waitingList) }
                }
            )
    }


    companion object {
        const val CALL_ROOM_STATE = "call_room_state"
    }
}

@Parcelize
data class CallRoomState(
    val isLoading: Boolean = false,
    val roomCode: String = "",
    val isHost: Boolean = false,
    val waitingParticipants: List<RoomParticipantUi> = emptyList(),
    val isShowDialogForWrongRoomCode: Boolean = false,
    val isShowDialogDisconnectRoom: Boolean = false,
    val isShowDialogPermission: Boolean = false
) : Parcelable, UiState

sealed interface CallRoomEvent : UiEvent {
    object WebSocketDisconnect : CallRoomEvent // 연결끊기
    data class CreateCallRoom(val roomCode: String) : CallRoomEvent //통화방생성
    data class DecideWaitingApproval(
        val userId: String,
        val isApprove: Boolean
    ) : CallRoomEvent // 웨이팅 승인 및 거절 요청

    object SwitchShowDialogForWrongRoomCode : CallRoomEvent
    object SwitchShowDisconnectRoomDialog : CallRoomEvent
    object SwitchShowDialogPermission : CallRoomEvent
}

sealed interface CallRoomEffect : UiEffect {
    data class SuccessCreateCallRoom(val roomCode: String) : CallRoomEffect // 방생성 성공결과
    data class NotifyNewWaitingMember(val roomCode: String, val updatedUser: RoomParticipantUi) :
        CallRoomEffect // 새 웨이팅 추가 알림 수신

    object SuccessDisconnectRoom : CallRoomEffect
}
