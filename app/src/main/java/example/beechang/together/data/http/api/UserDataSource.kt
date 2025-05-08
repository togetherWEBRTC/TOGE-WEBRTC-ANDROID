package example.beechang.together.data.http.api

import example.beechang.together.data.request.LoginRequest
import example.beechang.together.data.request.SignupRequest
import example.beechang.together.data.response.LoginResponse
import example.beechang.together.data.response.RefreshingAccessTokenResponse
import example.beechang.together.data.response.UserInfoResponse
import example.beechang.together.domain.data.TogeResult

interface UserDataSource {
    suspend fun requestLogin(loginRequest: LoginRequest): TogeResult<LoginResponse>
    suspend fun checkUsableId(userId: String): TogeResult<Boolean>
    suspend fun requestSignUp(
        signUpRequest: SignupRequest
    ): TogeResult<Boolean>

    suspend fun modifyProfileImage(): TogeResult<UserInfoResponse>
    suspend fun getUserInfo(): TogeResult<UserInfoResponse>
    suspend fun refreshingAccessToken(): TogeResult<RefreshingAccessTokenResponse>
}