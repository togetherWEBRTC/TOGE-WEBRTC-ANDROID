package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.RoomRepository
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class ChoiceDuplicateConnectionUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(forceDisconnectExisting: Boolean): TogeResult<Boolean> {
        val accessToken = userRepository.getAccessToken() ?: ""
        val sessionId = userRepository.getSessionId() ?: ""

        return roomRepository.choiceDuplicateConnection(
            forceDisconnectExisting = forceDisconnectExisting,
            accessToken = accessToken,
            sessionId = sessionId
        )
    }
}