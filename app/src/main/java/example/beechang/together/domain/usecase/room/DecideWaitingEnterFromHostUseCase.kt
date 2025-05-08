package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.RoomRepository
import javax.inject.Inject

class DecideWaitingEnterFromHostUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(
        roomCode: String,
        targetUserId: String,
        isAccept: Boolean
    ): TogeResult<Boolean> =
        roomRepository.requestDecisionWaitingEnter(roomCode, targetUserId, isAccept)
}