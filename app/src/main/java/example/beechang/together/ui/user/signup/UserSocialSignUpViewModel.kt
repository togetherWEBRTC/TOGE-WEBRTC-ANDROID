package example.beechang.together.ui.user.signup

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.user.RequestSocialSignUpUseCase
import example.beechang.together.domain.usecase.validation.ValidateNicknameUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class UserSocialSignUpViewModel @Inject constructor(
    private val requestSocialSignUpUseCase: RequestSocialSignUpUseCase,
    private val validateNicknameUseCase: ValidateNicknameUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<UserSocialSignUpUiState, UserSocialSignUpEvent, UserSocialSignUpEffect>(
    savedStateHandle, UserSocialSignUpUiState(), USER_SOCIAL_SIGN_UP_STATE
) {

    init {
        val token = savedStateHandle.get<String>("token") ?: "".also {
            if (it.isEmpty()) {
                sendEffect(UserSocialSignUpEffect.NeedToToken)
            }
        }
    }

    override fun onEvent(event: UserSocialSignUpEvent) {
        when (event) {
            is UserSocialSignUpEvent.NicknameChanged -> {
                viewModelScope.launch {
                    val isNicknameValid =
                        validateNicknameUseCase(event.nickname).getOrNull() ?: false
                    updateState {
                        copy(
                            nickname = event.nickname,
                            isNicknameValid = isNicknameValid
                        )
                    }
                    validateForm()
                }
            }

            is UserSocialSignUpEvent.TermsAgreementChanged -> {
                updateState { copy(termsAgreed = event.isChecked) }
                validateForm()
            }

            is UserSocialSignUpEvent.PrivacyAgreementChanged -> {
                updateState { copy(privacyAgreed = event.isChecked) }
                validateForm()
            }

            is UserSocialSignUpEvent.AllAgreementsChanged -> {
                updateState {
                    copy(
                        termsAgreed = event.isChecked,
                        privacyAgreed = event.isChecked
                    )
                }
                validateForm()
            }

            is UserSocialSignUpEvent.SubmitSignUp -> {
                if (!currentState.isSignUpEnabled) return
                val token = savedStateHandle.get<String>("token") ?: ""

                handleEvent(
                    action = {
                        requestSocialSignUpUseCase(
                            nickname = currentState.nickname,
                            token = token,
                            isAgreedTerms = currentState.termsAgreed,
                            isAgreedPrivacy = currentState.privacyAgreed
                        )
                    },
                    onSuccess = { result -> sendEffect(UserSocialSignUpEffect.SuccessSignUp) },
                    onStart = { updateState { copy(isLoading = true) } },
                    onFinally = { updateState { copy(isLoading = false) } }
                )

            }
        }
    }

    private fun validateForm() = viewModelScope.launch {
        currentState.run {
            updateState {
                copy(
                    isSignUpEnabled = termsAgreed && privacyAgreed && isNicknameValid
                )
            }
        }
    }

    companion object {
        private const val USER_SOCIAL_SIGN_UP_STATE = "USER_SOCIAL_SIGN_UP_STATE"
    }
}

@Parcelize
data class UserSocialSignUpUiState(
    val isLoading: Boolean = false,
    val nickname: String = "",
    val termsAgreed: Boolean = false,
    val privacyAgreed: Boolean = false,
    val isNicknameValid: Boolean = false,
    val isSignUpEnabled: Boolean = false,
) : Parcelable, UiState

sealed class UserSocialSignUpEvent : UiEvent {
    data class NicknameChanged(val nickname: String) : UserSocialSignUpEvent()
    data class TermsAgreementChanged(val isChecked: Boolean) : UserSocialSignUpEvent()
    data class PrivacyAgreementChanged(val isChecked: Boolean) : UserSocialSignUpEvent()
    data class AllAgreementsChanged(val isChecked: Boolean) : UserSocialSignUpEvent()
    object SubmitSignUp : UserSocialSignUpEvent()
}

sealed class UserSocialSignUpEffect : UiEffect {
    object NeedToToken : UserSocialSignUpEffect()
    object SuccessSignUp : UserSocialSignUpEffect()
}