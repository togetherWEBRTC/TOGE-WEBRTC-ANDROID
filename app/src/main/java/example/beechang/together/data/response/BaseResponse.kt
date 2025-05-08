package example.beechang.together.data.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String
) : TogeResponse

interface TogeResponse {
    val code: Int
    val message: String
    fun toSuccessBoolean(): Boolean {
        return code == 0
    }
}