package example.beechang.together.ui.user.signup

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.usecase.user.CheckUsableIdUseCase
import example.beechang.together.domain.usecase.user.RequestSignUpUseCase
import example.beechang.together.domain.usecase.validation.VaildatePasswordUseCase
import example.beechang.together.domain.usecase.validation.ValidateNicknameUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val checkUsableIdUseCase: CheckUsableIdUseCase,
    private val requestSignUpUseCase: RequestSignUpUseCase,
    private val validateNicknameUseCase: ValidateNicknameUseCase,
    private val validatePasswordUseCase: VaildatePasswordUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<SignUpUiState, SignUpEvent, SignUpEffect>(
    savedStateHandle, SignUpUiState(), SIGN_UP_STATE
) {

    public override fun onEvent(event: SignUpEvent) {
        viewModelScope.launch {
            when (event) {
                is SignUpEvent.UserIdChanged -> {
                    updateState {
                        copy(
                            userId = event.id,
                            isUsableId = false,
                            isDuplicateId = false
                        )
                    }
                    validateForm()
                }

                is SignUpEvent.NicknameChanged -> {
                    val isNicknameValid =
                        validateNicknameUseCase(event.nickname).getOrNull() ?: false
                    updateState {
                        copy(
                            nickname = event.nickname, isNicknameValid = isNicknameValid
                        )
                    }
                    validateForm()
                }

                is SignUpEvent.PasswordChanged -> {
                    val isPasswordValid =
                        validatePasswordUseCase.invoke(event.password).getOrNull() ?: false
                    updateState {
                        copy(
                            password = event.password, isPasswordValid = isPasswordValid
                        )
                    }
                    validateForm()
                }

                is SignUpEvent.ConfirmPasswordChanged -> {
                    updateState {
                        copy(
                            passwordConfirm = event.confirmPassword,
                            isPasswordMatching = currentState.password == event.confirmPassword
                        )
                    }
                    validateForm()
                }

                SignUpEvent.CheckUserId -> {
                    handleEvent(
                        action = { checkUsableIdUseCase.invoke(uiState.value.userId) },
                        onSuccess = {
                            updateState {
                                copy(
                                    isUsableId = it, isDuplicateId = false
                                )
                            }
                        },
                        onError = { error ->
                            updateState {
                                copy(
                                    isUsableId = false,
                                    isDuplicateId = (error.togeError == TogeError.DuplicatedId)
                                )
                            }
                        },
                        onStart = { updateState { copy(isLoading = true) } },
                        onFinally = {
                            validateForm()
                            updateState { copy(isLoading = false) }
                        }
                    )
                }

                SignUpEvent.SubmitSignUp -> {
                    handleEvent(
                        onStart = { updateState { copy(isLoading = true) } },
                        action = {
                            requestSignUpUseCase.invoke(
                                currentState.userId,
                                currentState.nickname,
                                currentState.password,
                                currentState.passwordConfirm
                            )
                        },
                        onSuccess = {
                            sendEffect(SignUpEffect.SuccessSignUp)
                        },
                        onError = { error ->
                            updateState {
                                copy(
                                    isUsableId = false,
                                    isDuplicateId = (error.togeError == TogeError.DuplicatedId),
                                    isPasswordMatching = (error.togeError != TogeError.PasswordNotMatch),
                                )
                            }
                        },
                        onFinally = { updateState { copy(isLoading = false) } }
                    )
                }
            }
        }
    }

    private fun validateForm() = viewModelScope.launch {
        currentState.run {
            updateState {
                copy(
                    isSignUpEnabled = isUsableId && isNicknameValid && isPasswordValid && isPasswordMatching
                )
            }
        }
    }


    companion object {
        private const val SIGN_UP_STATE = "signUpState"
    }
}

@Parcelize
data class SignUpUiState(
    val userId: String = "",
    val nickname: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val isUsableId: Boolean = false,
    val isDuplicateId: Boolean = false,
    val isNicknameValid: Boolean = false,
    val isPasswordValid: Boolean = false,
    val isPasswordMatching: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSignUpEnabled: Boolean = false
) : Parcelable, UiState

sealed class SignUpEvent : UiEvent {
    data class UserIdChanged(val id: String) : SignUpEvent()
    data class NicknameChanged(val nickname: String) : SignUpEvent()
    data class PasswordChanged(val password: String) : SignUpEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent()
    object CheckUserId : SignUpEvent()
    object SubmitSignUp : SignUpEvent()
}

sealed class SignUpEffect : UiEffect {
    object SuccessSignUp : SignUpEffect()
}