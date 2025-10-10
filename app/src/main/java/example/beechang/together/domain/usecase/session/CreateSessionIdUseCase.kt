package example.beechang.together.domain.usecase.session

import example.beechang.together.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

class CreateSessionIdUseCase @Inject constructor(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke() {
        val sessionId = UUID.randomUUID().toString()
        userRepository.saveSessionId(sessionId)
    }
}