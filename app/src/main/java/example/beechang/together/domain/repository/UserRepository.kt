package example.beechang.together.domain.repository

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.LoginState
import example.beechang.together.domain.model.SocialLoginResult
import example.beechang.together.domain.model.SocialLoginType
import example.beechang.together.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun requestLogin(userId: String, password: String): TogeResult<Boolean>
    suspend fun requestSocialLogin(
        token: String,
        type: SocialLoginType,
    ): TogeResult<SocialLoginResult>

    suspend fun checkUsableId(userId: String): TogeResult<Boolean>
    suspend fun requestSignUp(
        userId: String,
        nickname: String,
        password: String,
        passwordConfirm: String,
    ): TogeResult<Boolean>

    suspend fun requestSocialSignUp(
        token: String,
        nickname: String,
        isAgreedTerms: Boolean,
        isAgreedPrivacy: Boolean,
    ): TogeResult<Boolean>

    suspend fun modifyProfileImage(): TogeResult<Boolean>
    fun getLocalAccessTokenFlow(): Flow<TogeResult<String>>
    suspend fun getNewUserInfo(): TogeResult<UserInfo>
    suspend fun getUserInfo(): TogeResult<UserInfo>
    suspend fun getUserInfoFlow(): Flow<TogeResult<UserInfo>>
    suspend fun logout(): TogeResult<Boolean>
    suspend fun withdraw(): TogeResult<Boolean>
    suspend fun socialWithdraw(): TogeResult<Boolean>
    suspend fun getLoginStateFlow(): Flow<TogeResult<LoginState>>
    suspend fun updateAccessToken(): TogeResult<Boolean>
}