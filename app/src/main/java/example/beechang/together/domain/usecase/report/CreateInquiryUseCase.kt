package example.beechang.together.domain.usecase.report

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.InquiryCategory
import example.beechang.together.domain.repository.ReportRepository
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class CreateInquiryUseCase @Inject constructor(
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        content: String,
        category: InquiryCategory,
    ): TogeResult<Boolean> {
        val userInfo = userRepository.getUserInfo()
        val userId = when (userInfo) {
            is TogeResult.Success -> userInfo.data.userId
            else -> ""
        }

        return reportRepository.createInquiry(
            userId = userId,
            content = content,
            category = category
        )
    }
}