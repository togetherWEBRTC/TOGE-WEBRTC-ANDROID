package example.beechang.together.data.response

import example.beechang.together.domain.model.UserInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("accessToken") val accessToken: String? = null,
    @SerialName("refreshToken") val refreshToken: String? = null
) : TogeResponse

@Serializable
data class RefreshingAccessTokenResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("accessToken") val accessToken: String? = null
) : TogeResponse

@Serializable
data class UserInfoResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("userInfo") val userInfo: UserInfoDataResponse
) : TogeResponse

@Serializable
data class UserInfoDataResponse(
    @SerialName("userId") val userId: String = "",
    @SerialName("name") val nickname: String = "",
    @SerialName("profileUrl") val profileImageUrl: String = ""
) {
    fun toUserInfo(): UserInfo {
        return UserInfo(
            userId = userId,
            nickname = nickname,
            profileImageUrl = profileImageUrl
        )
    }
}