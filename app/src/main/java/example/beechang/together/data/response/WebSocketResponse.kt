package example.beechang.together.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class WebSocketEventResponse(
    val event: String, //SocketEventConstants
    val jsonData: String? = null
)

@Serializable
data class ChoiceDuplicateConnectionResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("connectionAllowed") val isAllowed: Boolean? = null,
) : TogeResponse

@Serializable
data class ConnectionCheckResponse(
    @SerialName("name") val name: String,
    @SerialName("connectionStatus") val connectionStatus: String,
    @SerialName("userState") val userState: String,
    @SerialName("message") val message: String,
    @SerialName("isDuplicateConnection") val isDuplicateConnection: Boolean? = null,
    @SerialName("existingSocketId") val existingSocketId: String? = null,
    @SerialName("currentSocketId") val currentSocketId: String? = null,
)