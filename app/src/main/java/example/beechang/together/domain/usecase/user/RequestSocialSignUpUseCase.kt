package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class RequestSocialSignUpUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(
        token: String,
        nickname: String,
        isAgreedTerms: Boolean,
        isAgreedPrivacy: Boolean,
    ) =
        userRepository.requestSocialSignUp(token, nickname, isAgreedTerms, isAgreedPrivacy)
}