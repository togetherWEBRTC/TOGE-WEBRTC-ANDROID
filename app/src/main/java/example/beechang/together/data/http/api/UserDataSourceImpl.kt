package example.beechang.together.data.http.api

import example.beechang.together.data.request.LoginRequest
import example.beechang.together.data.request.SignupRequest
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

    override suspend fun checkUsableId(userId: String): TogeResult<Boolean> {
        return apiToResult { userApi.checkUsableId(userId) }
            .map { it.toSuccessBoolean() }
    }

    override suspend fun requestSignUp(signUpRequest: SignupRequest): TogeResult<Boolean> {
        return apiToResult { userApi.signup(signUpRequest) }
            .map { it.toSuccessBoolean() }
    }

    override suspend fun modifyProfileImage(): TogeResult<UserInfoResponse> {
        return apiToResult { userApi.modifyProfileImage() }

    }

    override suspend fun getUserInfo(): TogeResult<UserInfoResponse> {
        return apiToResult { userApi.getUserInfo() }
    }

    override suspend fun refreshingAccessToken(): TogeResult<RefreshingAccessTokenResponse> {
        return apiToResult { userApi.refreshingAccessToken() }
    }
}



