package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.repository.RoomRepository
import javax.inject.Inject

class DisconnectRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke() = roomRepository.disconnect()
}