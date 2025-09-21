package example.beechang.together.data.http.api

import example.beechang.together.data.request.ReportUserRequest
import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.handler.apiToResult
import example.beechang.together.domain.data.TogeResult
import jakarta.inject.Inject

class ReportDataSourceImpl @Inject constructor(
    private val reportApi: ReportApi,
) : ReportDataSource {

    override suspend fun reportUser(
        reportedUserId: String,
        reportTargetContentType: String,
        reportTargetContentId: String,
        reasonCategory: String,
        reasonDetails: String?,
    ): TogeResult<BaseResponse> {
        return apiToResult {
            reportApi.reportUser(
                ReportUserRequest(
                    reportedUserId = reportedUserId,
                    reportTargetContentType = reportTargetContentType,
                    reportTargetContentId = reportTargetContentId,
                    reasonCategory = reasonCategory,
                    reasonDetails = reasonDetails
                )
            )
        }
    }
}