package example.beechang.together.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChoiceDuplicateConnectionResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("connectionAllowed") val isAllowed: Boolean? = null,
) : TogeResponse