package example.beechang.together.ui.call.waiting

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.room.DisconnectRoomUseCase
import example.beechang.together.domain.usecase.room.ReceiveWaitingResultUseCase
import example.beechang.together.domain.usecase.room.WaitingEnterRoomUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class CallWaitingViewModel @Inject constructor(
    private val disconnectRoomUseCase: DisconnectRoomUseCase,
    private val waitingEnterRoomUseCase: WaitingEnterRoomUseCase,
    private val receiveWaitingResultUseCase: ReceiveWaitingResultUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CallWaitingState, CallWaitingEvent, CallWaitingEffect>(
    savedStateHandle, CallWaitingState(), CALL_WAITING_STATE
) {

    init {
        val roomCode = savedStateHandle.get<String>("roomCode") ?: ""
        if (roomCode.isNotEmpty()) {
            requestEnterRoom(roomCode)
        } else {
            updateState { copy(isShowDialogForWrongRoomCode = true) }
        }

        listenWaitingResult()
        updateState { copy(roomCode = roomCode) }
    }

    override fun onEvent(event: CallWaitingEvent) {
        when (event) {
            CallWaitingEvent.Disconnect -> {
                disconnect()
            }

            is CallWaitingEvent.RequestEnterRoom -> {
                requestEnterRoom(event.roomCode)
            }

            CallWaitingEvent.SwitchWrongRoomCodeDialog -> {
                updateState { copy(isShowDialogForWrongRoomCode = !currentState.isShowDialogForWrongRoomCode) }
            }

            CallWaitingEvent.SwitchLeaveRoomDialog -> {
                updateState { copy(isShowDialogForLeaveRoom = !currentState.isShowDialogForLeaveRoom) }
            }
        }
    }

    private fun disconnect() = viewModelScope.launch {
        disconnectRoomUseCase.invoke()
    }

    private fun requestEnterRoom(roomCode: String) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { waitingEnterRoomUseCase(roomCode) },
            onSuccess = { result ->
                sendEffect(CallWaitingEffect.SuccessRequestEnterRoom)
            },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun listenWaitingResult() = viewModelScope.launch {
        receiveWaitingResultUseCase.invoke()
            .collectInViewModel(
                onSuccess = { result ->
                    if(result){
                        sendEffect(CallWaitingEffect.SuccessWaitingEnterRoom)
                    }else{
                        sendEffect(CallWaitingEffect.RejectedWaitingEnterRoom)
                    }
                }
            )
    }

    companion object {
        const val CALL_WAITING_STATE = "call_waiting_state"
    }
}


@Parcelize
data class CallWaitingState(
    val isLoading: Boolean = false,
    val roomCode: String = "",
    val isShowDialogForWrongRoomCode: Boolean = false,
    val isShowDialogForLeaveRoom: Boolean = false,
) : Parcelable, UiState

sealed interface CallWaitingEvent : UiEvent {
    object Disconnect : CallWaitingEvent
    data class RequestEnterRoom(val roomCode: String) : CallWaitingEvent // 통화방입장요청
    object SwitchWrongRoomCodeDialog : CallWaitingEvent //룸정보잘못됨 다이얼로그
    object SwitchLeaveRoomDialog : CallWaitingEvent // 방 나가기 다이얼로그
}

sealed interface CallWaitingEffect : UiEffect {
    object SuccessRequestEnterRoom : CallWaitingEffect // 방 웨이팅걸기 성공결과
    object RejectedWaitingEnterRoom : CallWaitingEffect // 방 웨이팅걸기 거절결과
    object SuccessWaitingEnterRoom : CallWaitingEffect // 방 웨이팅걸기 성공결과
}