package example.beechang.together.data.repository

import example.beechang.together.data.http.api.ReportDataSource
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.data.map
import example.beechang.together.domain.model.InquiryCategory
import example.beechang.together.domain.model.ReportReason
import example.beechang.together.domain.model.ReportType
import example.beechang.together.domain.repository.ReportRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val reportDataSource: ReportDataSource,
) : ReportRepository {

    override suspend fun reportUser(
        reportedUserId: String,
        reportTargetContentType: ReportType,
        reportTargetContentId: String,
        reasonCategory: ReportReason,
        reasonDetails: String?,
    ): TogeResult<Boolean> {
        return reportDataSource.reportUser(
            reportedUserId = reportedUserId,
            reportTargetContentType = reportTargetContentType.name,
            reportTargetContentId = reportTargetContentId,
            reasonCategory = reasonCategory.toCategoryFormat(),
            reasonDetails = reasonDetails
        ).map { it.toSuccessBoolean() }
    }

    override suspend fun createInquiry(
        userId: String,
        content: String,
        category: InquiryCategory,
    ): TogeResult<Boolean> {
        return reportDataSource.createInquiry(
            userId = userId,
            content = content,
            category = category.code
        ).map { it.toSuccessBoolean() }
    }
}