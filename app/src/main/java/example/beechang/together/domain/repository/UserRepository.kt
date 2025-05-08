package example.beechang.together.domain.repository

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.LoginState
import example.beechang.together.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun requestLogin(userId: String, password: String): TogeResult<Boolean>
    suspend fun checkUsableId(userId: String): TogeResult<Boolean>
    suspend fun requestSignUp(
        userId: String,
        nickname: String,
        password: String,
        passwordConfirm: String
    ): TogeResult<Boolean>

    suspend fun modifyProfileImage(): TogeResult<Boolean>
    fun getLocalAccessTokenFlow(): Flow<TogeResult<String>>
    suspend fun getNewUserInfo(): TogeResult<UserInfo>
    suspend fun getUserInfo(): TogeResult<UserInfo>
    suspend fun getUserInfoFlow(): Flow<TogeResult<UserInfo>>
    suspend fun logout(): TogeResult<Boolean>
    suspend fun getLoginStateFlow(): Flow<TogeResult<LoginState>>
    suspend fun updateAccessToken(): TogeResult<Boolean>
}