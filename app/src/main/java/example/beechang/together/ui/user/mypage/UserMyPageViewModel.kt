package example.beechang.together.ui.user.mypage

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import example.beechang.together.domain.usecase.user.GetLoginStateUseCase
import example.beechang.together.domain.usecase.user.GetNewProfileImageUseCase
import example.beechang.together.domain.usecase.user.GetUserInfoUseCase
import example.beechang.together.domain.usecase.user.RequestLogoutUseCase
import example.beechang.together.ui.utils.BaseViewModel
import example.beechang.together.ui.utils.UiEffect
import example.beechang.together.ui.utils.UiEvent
import example.beechang.together.ui.utils.UiState
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class UserMyPageViewModel @Inject constructor(
    private val getNewProfileImageUseCase: GetNewProfileImageUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val requestLogoutUseCase: RequestLogoutUseCase,
    private val getLoginStateUseCase: GetLoginStateUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<UserMyPageState, UserMyPageEvent, UserMyPageEffect>(
    savedStateHandle, UserMyPageState(), USER_MY_PAGE_STATE
) {
    public override fun onEvent(event: UserMyPageEvent) {
        when (event) {
            UserMyPageEvent.OnDoubleCheckModifyProfileImage -> {
                updateState { copy(isShowDoubleCheckModifyProfileImage = true) }
            }

            UserMyPageEvent.OnModifyProfileImage -> {
                updateState { copy(isShowDoubleCheckModifyProfileImage = false) }
                getNewProfileImage()
            }

            UserMyPageEvent.OnCancelModifyProfileImage -> {
                updateState { copy(isShowDoubleCheckModifyProfileImage = false) }
            }

            UserMyPageEvent.OnLogout -> {
                logout()
            }
        }
    }

    init {
        getLoginState()
        getUserInfo()
    }

    private fun getLoginState() = viewModelScope.launch {
        getLoginStateUseCase.invoke()
            .collect { res ->
                res.onSuccess {
                    updateLoginState(it)
                }.onError { togeError, _, _ ->
                    togeError?.let { sendError(togeError = togeError) }
                }
            }
    }

    private fun getUserInfo() = viewModelScope.launch {
        getUserInfoUseCase.observeUserInfo()
            .onStart { updateState { copy(isLoading = true) } }
            .collect { res ->
                res.onSuccess {
                    updateState {
                        copy(
                            userId = it.userId,
                            nickname = it.nickname,
                            profileImageUrl = it.profileImageUrl,
                        )
                    }
                }.onError { togeError, _, _ ->
                    togeError?.let { sendError(togeError = togeError) }
                }
                updateState { copy(isLoading = false) }
            }
    }

    private fun getNewProfileImage() =
        handleEvent(
            action = { getNewProfileImageUseCase.invoke() },
            onStart = { updateState { copy(isLoading = true) } },
            onSuccess = { sendEffect(UserMyPageEffect.SuccessModifyProfileImage) },
            onFinally = { updateState { copy(isLoading = false) } },
        )

    private fun logout() =
        handleEvent(
            action = { requestLogoutUseCase.invoke() },
            onStart = { updateState { copy(isLoading = true) } },
            onSuccess = {},
            onFinally = { updateState { copy(isLoading = false) } },
        )


    companion object {
        private const val USER_MY_PAGE_STATE = "userMyPageState"
    }
}

@Parcelize
data class UserMyPageState(
    val userId: String = "",
    val nickname: String = "",
    val profileImageUrl: String = "",
    val isShowDoubleCheckModifyProfileImage: Boolean = false,
    val isLoading: Boolean = false
) : Parcelable, UiState

sealed class UserMyPageEvent : UiEvent {
    object OnDoubleCheckModifyProfileImage : UserMyPageEvent()
    object OnModifyProfileImage : UserMyPageEvent()
    object OnCancelModifyProfileImage : UserMyPageEvent()
    object OnLogout : UserMyPageEvent()
}

sealed class UserMyPageEffect : UiEffect {
    object SuccessModifyProfileImage : UserMyPageEffect()
}