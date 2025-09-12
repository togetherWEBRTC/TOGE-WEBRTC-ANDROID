package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.repository.RoomRepository
import javax.inject.Inject

class ExpelMemberUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(roomId: String, targetMemberId: String) =
        roomRepository.expelMemberFromRoom(roomId, targetMemberId)
}