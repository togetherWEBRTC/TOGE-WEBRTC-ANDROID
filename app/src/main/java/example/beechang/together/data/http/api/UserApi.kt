package example.beechang.together.data.http.api

import example.beechang.together.data.request.LoginRequest
import example.beechang.together.data.request.SignupRequest
import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.LoginResponse
import example.beechang.together.data.response.RefreshingAccessTokenResponse
import example.beechang.together.data.response.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<BaseResponse>

    @POST("api/auth/refresh/token")
    suspend fun refreshingAccessToken(): Response<RefreshingAccessTokenResponse>

    @GET("api/auth/usable-id/{userId}")
    suspend fun checkUsableId(
        @Path("userId") userId: String
    ): Response<BaseResponse>

    @GET("api/auth/user-info")
    suspend fun getUserInfo(): Response<UserInfoResponse>

    @POST("api/auth/modify/profile-image")
    suspend fun modifyProfileImage(): Response<UserInfoResponse>
}