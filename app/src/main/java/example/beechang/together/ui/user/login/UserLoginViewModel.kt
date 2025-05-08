package example.beechang.together.ui.user.login

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.user.RequestLoginUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class UserLoginViewModel @Inject constructor(
    private val requestLoginUseCase: RequestLoginUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<UserLoginState, UserLoginEvent, UserLoginEffect>(
    savedStateHandle, UserLoginState(), USER_LOGIN_STATE
) {
    public override fun onEvent(event: UserLoginEvent) {
        viewModelScope.launch {
            when (event) {
                is UserLoginEvent.Login -> {
                    handleEvent(
                        action = { requestLoginUseCase.invoke(event.userId, event.password) },
                        onSuccess = { effect -> sendEffect(UserLoginEffect.LoginSuccess) },
                        onError = { error -> },
                        onStart = { updateState { copy(isLoading = true) } },
                        onFinally = { updateState { copy(isLoading = false) } }
                    )
                }

                is UserLoginEvent.PasswordChanged -> {
                    updateState { copy(inputPassword = event.password) }
                }

                is UserLoginEvent.UserLoginIdChanged -> {
                    updateState { copy(inputUserId = event.id) }
                }
            }
        }
    }


    companion object {
        private const val USER_LOGIN_STATE = "userLoginState"
    }
}

@Parcelize
data class UserLoginState(
    val inputUserId: String = "",
    val inputPassword: String = "",
    val isLoading: Boolean = false
) : Parcelable, UiState {
    val isLoginButtonEnabled: Boolean
        get() = inputUserId.isNotEmpty() && inputPassword.isNotEmpty()
}

sealed class UserLoginEvent : UiEvent {
    data class Login(val userId: String, val password: String) : UserLoginEvent()
    data class UserLoginIdChanged(val id: String) : UserLoginEvent()
    data class PasswordChanged(val password: String) : UserLoginEvent()
}

sealed class UserLoginEffect : UiEffect {
    object LoginSuccess : UserLoginEffect()
}