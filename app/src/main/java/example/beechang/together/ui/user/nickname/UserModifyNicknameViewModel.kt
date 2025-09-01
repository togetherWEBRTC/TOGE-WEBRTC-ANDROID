package example.beechang.together.ui.user.nickname

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.user.RequestModifyNicknameUseCase
import example.beechang.together.domain.usecase.validation.ValidateNicknameUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class UserModifyNicknameViewModel @Inject constructor(
    private val validateNicknameUseCase: ValidateNicknameUseCase,
    private val requestModifyNicknameUseCase: RequestModifyNicknameUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<UserModifyNicknameState, UserModifyNicknameEvent, UserModifyNicknameEffect>(
    savedStateHandle, UserModifyNicknameState(), USER_MODIFY_NICKNAME_STATE
) {

    override fun onEvent(event: UserModifyNicknameEvent) {
        viewModelScope.launch {
            when (event) {
                is UserModifyNicknameEvent.NicknameChanged -> {
                    val isNicknameValid =
                        validateNicknameUseCase(event.nickname).getOrNull() ?: false
                    updateState {
                        copy(
                            nickname = event.nickname, isNicknameValid = isNicknameValid
                        )
                    }
                }

                UserModifyNicknameEvent.SubmitNickname -> {
                    val isNicknameValid =
                        validateNicknameUseCase(currentState.nickname).getOrNull() ?: false
                    if (!isNicknameValid) {
                        return@launch
                    }
                    handleEvent(
                        onStart = { updateState { copy(isLoading = true) } },
                        action = { requestModifyNicknameUseCase.invoke(currentState.nickname) },
                        onSuccess = {
                            sendEffect(UserModifyNicknameEffect.SuccessModifyNickname)
                        },
                        onFinally = { updateState { copy(isLoading = false) } }
                    )
                }
            }
        }
    }

    companion object {
        private const val USER_MODIFY_NICKNAME_STATE = "USER_MODIFY_NICKNAME_STATE"
    }
}

@Parcelize
data class UserModifyNicknameState(
    val nickname: String = "",
    val isNicknameValid: Boolean = false,
    val isLoading: Boolean = false,
) : Parcelable, UiState

sealed class UserModifyNicknameEvent : UiEvent {
    data class NicknameChanged(val nickname: String) : UserModifyNicknameEvent()
    object SubmitNickname : UserModifyNicknameEvent()
}

sealed class UserModifyNicknameEffect : UiEffect {
    object SuccessModifyNickname : UserModifyNicknameEffect()
}