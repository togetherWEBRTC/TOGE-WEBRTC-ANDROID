package example.beechang.together.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("id") val id: String,
    @SerialName("password") val password: String
)

@Serializable
data class SocialLoginRequest(
    @SerialName("token") val token: String,
    @SerialName("type") val type: String
)

@Serializable
data class SocialSignUpRequest(
    @SerialName("token") val token: String,
    @SerialName("nickname") val nickname: String,
    @SerialName("isAgreedTerms") val isAgreedTerms: Boolean,
    @SerialName("isAgreedPrivacy") val isAgreedPrivacy: Boolean
)

@Serializable
data class SignupRequest(
    @SerialName("userId") val userId: String,
    @SerialName("nickname") val nickname: String,
    @SerialName("password") val password: String,
    @SerialName("passwordConfirm") val passwordConfirm: String
)