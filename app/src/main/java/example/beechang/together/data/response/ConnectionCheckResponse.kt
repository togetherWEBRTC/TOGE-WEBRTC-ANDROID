package example.beechang.together.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionCheckResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("isDuplicateConnection") val isDuplicateConnection: Boolean? = null,
    @SerialName("existingSocketId") val existingSocketId: String? = null,
    @SerialName("currentSocketId") val currentSocketId: String? = null,
) : TogeResponse