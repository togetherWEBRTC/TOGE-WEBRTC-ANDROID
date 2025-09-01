package example.beechang.together.data.http.api

import example.beechang.together.data.request.LoginRequest
import example.beechang.together.data.request.ModifyNicknameRequest
import example.beechang.together.data.request.SignupRequest
import example.beechang.together.data.request.SocialLoginRequest
import example.beechang.together.data.request.SocialSignUpRequest
import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.LoginResponse
import example.beechang.together.data.response.RefreshingAccessTokenResponse
import example.beechang.together.data.response.UserInfoResponse
import example.beechang.together.domain.data.TogeResult

interface UserDataSource {
    suspend fun requestLogin(loginRequest: LoginRequest): TogeResult<LoginResponse>
    suspend fun requestSocialLogin(socialLoginRequest: SocialLoginRequest): TogeResult<LoginResponse>
    suspend fun checkUsableId(userId: String): TogeResult<Boolean>
    suspend fun requestSignUp(signUpRequest: SignupRequest): TogeResult<Boolean>
    suspend fun requestSocialSignUp(socialSignUpRequest: SocialSignUpRequest): TogeResult<LoginResponse>
    suspend fun modifyProfileImage(): TogeResult<UserInfoResponse>
    suspend fun modifyNickname(modifyNicknameRequest: ModifyNicknameRequest): TogeResult<UserInfoResponse>
    suspend fun getUserInfo(): TogeResult<UserInfoResponse>
    suspend fun refreshingAccessToken(): TogeResult<RefreshingAccessTokenResponse>
    suspend fun withdraw(): TogeResult<BaseResponse>
    suspend fun socialWithdraw(): TogeResult<BaseResponse>
}