package example.beechang.together.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReportUserRequest(
    @SerialName("reportedUserId") val reportedUserId: String,
    @SerialName("reportTargetContentType") val reportTargetContentType: String,
    @SerialName("reportTargetContentId") val reportTargetContentId: String,
    @SerialName("reasonCategory") val reasonCategory: String,
    @SerialName("reasonDetails") val reasonDetails: String?,
)