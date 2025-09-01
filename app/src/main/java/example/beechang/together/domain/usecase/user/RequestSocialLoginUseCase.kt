package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.model.SocialLoginType
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class RequestSocialLoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(token: String, type: SocialLoginType) =
        userRepository.requestSocialLogin(token, type)
}