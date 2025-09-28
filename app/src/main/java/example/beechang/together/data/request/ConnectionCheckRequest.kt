package example.beechang.together.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionCheckRequest(
    @SerialName("accessToken") val accessToken: String,
    @SerialName("sessionId") val sessionId: String
)