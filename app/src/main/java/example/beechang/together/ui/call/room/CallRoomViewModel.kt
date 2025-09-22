package example.beechang.together.ui.call.room

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.usecase.room.CreateRoomUseCase
import example.beechang.together.domain.usecase.room.DecideWaitingEnterFromHostUseCase
import example.beechang.together.domain.usecase.room.DisconnectRoomUseCase
import example.beechang.together.domain.usecase.room.ExpelMemberUseCase
import example.beechang.together.domain.usecase.room.ReceiveExpelledNotifyUseCase
import example.beechang.together.domain.usecase.room.ReceiveRoomDisconnectedUseCase
import example.beechang.together.domain.usecase.room.ReceiveWaitingNotifyUseCase
import example.beechang.together.domain.usecase.report.ReportUserUseCase
import example.beechang.together.domain.usecase.report.CreateInquiryUseCase
import example.beechang.together.domain.model.ReportReason
import example.beechang.together.domain.model.ReportType
import example.beechang.together.domain.model.InquiryCategory
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
    private val receiveExpelledNotifyUseCase: ReceiveExpelledNotifyUseCase,
    private val receiveRoomDisconnectedUseCase: ReceiveRoomDisconnectedUseCase,
    private val expelMemberUseCase: ExpelMemberUseCase,
    private val reportUserUseCase: ReportUserUseCase,
    private val createInquiryUseCase: CreateInquiryUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<CallRoomState, CallRoomEvent, CallRoomEffect>(
    savedStateHandle, CallRoomState(), CALL_ROOM_STATE
) {
    init {
        val isHost = savedStateHandle.get<Boolean>("isHost") ?: false
        val roomCodeFromArgs = savedStateHandle.get<String>("roomCode")
        if (roomCodeFromArgs.isNullOrEmpty()) {
            updateState { copy(isHost = isHost) }
        } else {
            updateState { copy(roomCode = roomCodeFromArgs, isHost = isHost) }
        }

        listeningWaitingNotify()
        listeningRoomConnectionState()
        listeningBeExpelledNotify()
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

            is CallRoomEvent.ExpelMember -> {
                expelMember(targetMemberId = event.targetMemberId)
            }

            is CallRoomEvent.ReportUser -> {
                reportUser(
                    reportedUserId = event.reportedUserId,
                    reasonCategory = event.reasonCategory,
                    reasonDetails = event.reasonDetails
                )
            }

            is CallRoomEvent.CreateInquiry -> {
                createInquiry(
                    category = event.category,
                    content = event.content
                )
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

    private fun expelMember(targetMemberId: String) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { expelMemberUseCase(currentState.roomCode, targetMemberId) },
            onSuccess = {
                sendEffect(CallRoomEffect.SuccessExpelMember)
            },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun reportUser(
        reportedUserId: String,
        reasonCategory: ReportReason,
        reasonDetails: String?,
    ) = handleEvent(
        onStart = { updateState { copy(isLoading = true) } },
        action = {
            val roomCode = currentState.roomCode
            reportUserUseCase(
                reportedUserId = reportedUserId,
                reportTargetContentType = ReportType.CALL,
                reportTargetContentId = roomCode,
                reasonCategory = reasonCategory,
                reasonDetails = reasonDetails
            )
        },
        onSuccess = {
            sendEffect(CallRoomEffect.SuccessReportUser)
        },
        onFinally = { updateState { copy(isLoading = false) } }
    )

    private fun createInquiry(
        category: InquiryCategory,
        content: String,
    ) = handleEvent(
        onStart = { updateState { copy(isLoading = true) } },
        action = { createInquiryUseCase.invoke(content = content, category = category) },
        onSuccess = { sendEffect(CallRoomEffect.SuccessCreateInquiry) },
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

    private fun listeningBeExpelledNotify() = viewModelScope.launch {
        receiveExpelledNotifyUseCase.invoke()
            .collectInViewModel(
                onSuccess = { result ->
                    sendEffect(CallRoomEffect.BeExpelledRoom)
                }
            )
    }

    private fun listeningRoomConnectionState() = viewModelScope.launch {
        receiveRoomDisconnectedUseCase.invoke()
            .collect {
                sendError(TogeError.FailedToConnectRoom)
            }
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
) : Parcelable, UiState

sealed interface CallRoomEvent : UiEvent {
    object WebSocketDisconnect : CallRoomEvent // 연결끊기
    data class CreateCallRoom(val roomCode: String) : CallRoomEvent //통화방생성
    data class DecideWaitingApproval(
        val userId: String,
        val isApprove: Boolean,
    ) : CallRoomEvent // 웨이팅 승인 및 거절 요청

    data class ExpelMember(val targetMemberId: String) : CallRoomEvent
    data class ReportUser(
        val reportedUserId: String,
        val reasonCategory: ReportReason,
        val reasonDetails: String?,
    ) : CallRoomEvent

    data class CreateInquiry(
        val category: InquiryCategory,
        val content: String,
    ) : CallRoomEvent
}

sealed interface CallRoomEffect : UiEffect {
    data class SuccessCreateCallRoom(val roomCode: String) : CallRoomEffect // 방생성 성공결과
    data class NotifyNewWaitingMember(val roomCode: String, val updatedUser: RoomParticipantUi) :
        CallRoomEffect // 새 웨이팅 추가 알림 수신

    object SuccessDisconnectRoom : CallRoomEffect
    object SuccessExpelMember : CallRoomEffect
    object BeExpelledRoom : CallRoomEffect
    object SuccessReportUser : CallRoomEffect
    object SuccessCreateInquiry : CallRoomEffect
}
