package example.beechang.together.ui.home

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.room.ConnectRoomUseCase
import example.beechang.together.domain.usecase.room.ChoiceDuplicateConnectionUseCase
import example.beechang.together.domain.usecase.report.CreateInquiryUseCase
import example.beechang.together.domain.usecase.user.GetLoginStateUseCase
import example.beechang.together.domain.usecase.user.GetUserInfoUseCase
import example.beechang.together.domain.usecase.user.UpdateAccessTokenUseCase
import example.beechang.together.domain.model.InquiryCategory
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getLoginStateUseCase: GetLoginStateUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val updateAccessTokenUseCase: UpdateAccessTokenUseCase,
    private val connectRoomUseCase: ConnectRoomUseCase,
    private val choiceDuplicateConnectionUseCase: ChoiceDuplicateConnectionUseCase,
    private val createInquiryUseCase: CreateInquiryUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<HomeState, HomeEvent, HomeEffect>(
    savedStateHandle, HomeState(), HOME_STATE
) {
    public override fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.CrateRoom -> {
                updateAccessToken(true)
            }

            HomeEvent.EnterRoom -> {
                updateAccessToken(false)
            }

            is HomeEvent.UpdateEnterRoomCode -> {
                updateState { copy(enterRoomCode = event.code) }
            }

            is HomeEvent.CreateInquiry -> {
                createInquiry(event.category, event.content)
            }

            HomeEvent.DismissDuplicateConnectionDialog -> {
                updateState {
                    copy(
                        showDuplicateConnectionDialog = false,
                        pendingRoomCreation = false,
                        pendingRoomCode = ""
                    )
                }
            }

            is HomeEvent.HandleDuplicateConnectionChoice -> {
                handleDuplicateConnectionChoice(event.forceDisconnectExisting)
            }
        }
    }

    init {
        observeLoginState()
        observeUserInfo()
    }

    private fun observeLoginState() = viewModelScope.launch {
        updateState { copy(isLoading = true) }
        getLoginStateUseCase.invoke()
            .collectInViewModel(
                onSuccess = {
                    viewModelScope.launch {
                        updateLoginState(it).join()
                        updateState { copy(isShowSkeleton = false) }
                    }
                },
                onFinally = { updateState { copy(isLoading = false) } }
            )
    }

    private fun observeUserInfo() = viewModelScope.launch {
        updateState { copy(isLoading = true) }
        getUserInfoUseCase.observeUserInfo()
            .collectInViewModel(
                onSuccess = { updateState { copy(profileUrl = it.profileImageUrl) } },
                onFinally = { updateState { copy(isLoading = false) } }
            )
    }

    private fun updateAccessToken(isCratingRoom: Boolean) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { updateAccessTokenUseCase.invoke() },
            onSuccess = { connectRoom(isCratingRoom) },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun connectRoom(isCreatingRoom: Boolean) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { connectRoomUseCase.invoke() },
            onSuccess = { connectionResult ->
                if (connectionResult.isDuplicateConnection) {  // 중복 연결 감지
                    updateState {
                        copy(
                            showDuplicateConnectionDialog = true,
                            pendingRoomCreation = isCreatingRoom,
                            pendingRoomCode = if (isCreatingRoom) "" else enterRoomCode
                        )
                    }
                } else {
                    moveToRoom(isCreatingRoom)
                }
            },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun handleDuplicateConnectionChoice(forceDisconnectExisting: Boolean) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { choiceDuplicateConnectionUseCase.invoke(forceDisconnectExisting) },
            onSuccess = { connectionAllowed ->
                updateState { copy(showDuplicateConnectionDialog = false) }

                if (connectionAllowed) {
                    // 연결이 허용됨 - 방으로 이동
                    val isCreatingRoom = uiState.value.pendingRoomCreation
                    moveToRoom(isCreatingRoom)
                } else {
                    updateState {
                        copy(
                            pendingRoomCreation = false,
                            pendingRoomCode = ""
                        )
                    }
                }
            },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    private fun moveToRoom(isCreatingRoom: Boolean) {
        if (isCreatingRoom) {
            sendEffect(HomeEffect.ReadyMoveToRoom("", true))
        } else {
            val roomCode = uiState.value.pendingRoomCode.ifEmpty { uiState.value.enterRoomCode }
            sendEffect(HomeEffect.ReadyMoveToRoom(roomCode, false))
            updateState { copy(enterRoomCode = "") }
        }

        updateState {
            copy(
                pendingRoomCreation = false,
                pendingRoomCode = ""
            )
        }
    }

    private fun createInquiry(category: InquiryCategory, content: String) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { createInquiryUseCase.invoke(content, category) },
            onSuccess = { sendEffect(HomeEffect.InquirySuccess) },
            onFinally = { updateState { copy(isLoading = false) } }
        )

    companion object {
        private const val HOME_STATE = "homeState"
    }
}

@Parcelize
data class HomeState(
    val isLoading: Boolean = false,
    val profileUrl: String = "",
    val enterRoomCode: String = "",
    val isShowSkeleton: Boolean = true,
    val showDuplicateConnectionDialog: Boolean = false,
    val pendingRoomCreation: Boolean = false,
    val pendingRoomCode: String = "",
) : Parcelable, UiState

sealed interface HomeEvent : UiEvent {
    object CrateRoom : HomeEvent
    object EnterRoom : HomeEvent
    data class UpdateEnterRoomCode(val code: String) : HomeEvent
    data class CreateInquiry(val category: InquiryCategory, val content: String) : HomeEvent
    object DismissDuplicateConnectionDialog : HomeEvent
    data class HandleDuplicateConnectionChoice(val forceDisconnectExisting: Boolean) : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data class ReadyMoveToRoom(val roomCode: String, val isHost: Boolean) : HomeEffect
    object InquirySuccess : HomeEffect
}