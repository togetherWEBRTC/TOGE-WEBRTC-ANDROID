package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomCode
import example.beechang.together.domain.repository.RoomRepository
import javax.inject.Inject

class CreateRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(roomCode: String? = null): TogeResult<RoomCode> =
        roomRepository.createRoom(roomCode)
}