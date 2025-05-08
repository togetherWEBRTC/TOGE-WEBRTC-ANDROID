package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class CheckUsableIdUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(username: String): TogeResult<Boolean> =
        userRepository.checkUsableId(username)
}