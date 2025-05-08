package example.beechang.together.ui.home

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.room.ConnectRoomUseCase
import example.beechang.together.domain.usecase.user.GetLoginStateUseCase
import example.beechang.together.domain.usecase.user.GetUserInfoUseCase
import example.beechang.together.domain.usecase.user.UpdateAccessTokenUseCase
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
    savedStateHandle: SavedStateHandle
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
                onSuccess = { updateLoginState(it) },
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

    private fun connectRoom(isCratingRoom: Boolean) =
        handleEvent(
            onStart = { updateState { copy(isLoading = true) } },
            action = { connectRoomUseCase.invoke() },
            onSuccess = {
                if (isCratingRoom) {
                    sendEffect(HomeEffect.ReadyMoveToRoom("", true))
                } else {
                    val roomCode = uiState.value.enterRoomCode
                    sendEffect(HomeEffect.ReadyMoveToRoom(roomCode, false))
                    updateState { copy(enterRoomCode = "") }
                }
            },
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
) : Parcelable, UiState

sealed interface HomeEvent : UiEvent {
    object CrateRoom : HomeEvent
    object EnterRoom : HomeEvent
    data class UpdateEnterRoomCode(val code: String) : HomeEvent
}

sealed interface HomeEffect : UiEffect {
    data class ReadyMoveToRoom(val roomCode: String, val isHost: Boolean) : HomeEffect
}