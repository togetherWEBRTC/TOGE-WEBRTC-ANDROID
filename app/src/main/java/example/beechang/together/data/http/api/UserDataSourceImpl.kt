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
import example.beechang.together.data.response.handler.apiToResult
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.data.map
import jakarta.inject.Inject


class UserDataSourceImpl @Inject constructor(
    private val userApi: UserApi
) : UserDataSource {

    override suspend fun requestLogin(loginRequest: LoginRequest): TogeResult<LoginResponse> {
        return apiToResult { userApi.login(loginRequest) }
    }

    override suspend fun requestSocialLogin(socialLoginRequest: SocialLoginRequest): TogeResult<LoginResponse> {
        return apiToResult { userApi.socialLogin(socialLoginRequest) }
    }

    override suspend fun checkUsableId(userId: String): TogeResult<Boolean> {
        return apiToResult { userApi.checkUsableId(userId) }
            .map { it.toSuccessBoolean() }
    }

    override suspend fun requestSignUp(signUpRequest: SignupRequest): TogeResult<Boolean> {
        return apiToResult { userApi.signup(signUpRequest) }
            .map { it.toSuccessBoolean() }
    }

    override suspend fun requestSocialSignUp(socialSignUpRequest: SocialSignUpRequest): TogeResult<LoginResponse> {
        return apiToResult { userApi.socialSignUp(socialSignUpRequest) }
    }

    override suspend fun modifyProfileImage(): TogeResult<UserInfoResponse> {
        return apiToResult { userApi.modifyProfileImage() }
    }

    override suspend fun modifyNickname(modifyNicknameRequest: ModifyNicknameRequest): TogeResult<UserInfoResponse> {
        return apiToResult { userApi.modifyNickname(modifyNicknameRequest) }
    }

    override suspend fun getUserInfo(): TogeResult<UserInfoResponse> {
        return apiToResult { userApi.getUserInfo() }
    }

    override suspend fun refreshingAccessToken(): TogeResult<RefreshingAccessTokenResponse> {
        return apiToResult { userApi.refreshingAccessToken() }
    }

    override suspend fun withdraw(): TogeResult<BaseResponse> {
        return apiToResult { userApi.withdraw() }
    }

    override suspend fun socialWithdraw(): TogeResult<BaseResponse> {
        return apiToResult { userApi.socialWithdraw() }
    }
}



