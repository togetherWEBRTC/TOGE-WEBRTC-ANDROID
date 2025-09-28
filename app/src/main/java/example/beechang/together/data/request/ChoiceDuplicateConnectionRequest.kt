package example.beechang.together.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChoiceDuplicateConnectionRequest(
    @SerialName("forceDisconnectExisting") val forceDisconnect: Boolean,
    @SerialName("accessToken") val accessToken: String,
    @SerialName("sessionId") val sessionId: String,
)