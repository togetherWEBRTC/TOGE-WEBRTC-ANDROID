package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class RequestSignUpUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        userId: String,
        nickname: String,
        password: String,
        passwordConfirm: String
    ): TogeResult<Boolean> =
        userRepository.requestSignUp(userId, nickname, password, passwordConfirm)
}