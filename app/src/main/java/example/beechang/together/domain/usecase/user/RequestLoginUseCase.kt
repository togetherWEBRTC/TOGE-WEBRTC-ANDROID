package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class RequestLoginUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, password: String) =
        userRepository.requestLogin(userId, password)
}