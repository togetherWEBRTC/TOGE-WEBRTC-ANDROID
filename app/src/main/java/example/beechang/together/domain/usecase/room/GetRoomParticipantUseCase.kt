package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.repository.RoomRepository
import javax.inject.Inject

class GetRoomParticipantUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomCode: String, isIncludingMySelf: Boolean): TogeResult<List<RoomParticipant>> =
        roomRepository.getRoomParticipant(roomCode, isIncludingMySelf)
}