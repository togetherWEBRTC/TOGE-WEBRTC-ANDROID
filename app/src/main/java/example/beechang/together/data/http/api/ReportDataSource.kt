package example.beechang.together.data.http.api

import example.beechang.together.data.response.BaseResponse
import example.beechang.together.domain.data.TogeResult

interface ReportDataSource {
    suspend fun reportUser(
        reportedUserId: String,
        reportTargetContentType: String,
        reportTargetContentId: String,
        reasonCategory: String,
        reasonDetails: String?,
    ): TogeResult<BaseResponse>
}