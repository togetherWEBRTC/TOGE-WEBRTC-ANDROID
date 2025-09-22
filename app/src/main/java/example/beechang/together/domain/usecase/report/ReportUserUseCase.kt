package example.beechang.together.domain.usecase.report

import example.beechang.together.domain.model.ReportReason
import example.beechang.together.domain.model.ReportType
import example.beechang.together.domain.repository.ReportRepository
import javax.inject.Inject

class ReportUserUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
) {
    suspend operator fun invoke(
        reportedUserId: String,
        reportTargetContentType: ReportType,
        reportTargetContentId: String,
        reasonCategory: ReportReason,
        reasonDetails: String?,
    ) = reportRepository.reportUser(
        reportedUserId = reportedUserId,
        reportTargetContentType = reportTargetContentType,
        reportTargetContentId = reportTargetContentId,
        reasonCategory = reasonCategory,
        reasonDetails = reasonDetails
    )
}