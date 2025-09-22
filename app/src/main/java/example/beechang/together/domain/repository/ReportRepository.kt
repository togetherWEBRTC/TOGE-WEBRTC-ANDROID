package example.beechang.together.domain.repository

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.InquiryCategory
import example.beechang.together.domain.model.ReportReason
import example.beechang.together.domain.model.ReportType

interface ReportRepository {
    suspend fun reportUser(
        reportedUserId: String,
        reportTargetContentType: ReportType,
        reportTargetContentId: String,
        reasonCategory: ReportReason,
        reasonDetails: String?,
    ): TogeResult<Boolean>

    suspend fun createInquiry(
        userId: String,
        content: String,
        category: InquiryCategory,
    ): TogeResult<Boolean>
}