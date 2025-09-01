package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class RequestModifyNicknameUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(nickname: String) = userRepository.modifyNickname(nickname)
}
