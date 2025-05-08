package example.beechang.together.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("id") val id: String,
    @SerialName("password") val password: String
)

@Serializable
data class SignupRequest(
    @SerialName("userId") val userId: String,
    @SerialName("nickname") val nickname: String,
    @SerialName("password") val password: String,
    @SerialName("passwordConfirm") val passwordConfirm: String
)