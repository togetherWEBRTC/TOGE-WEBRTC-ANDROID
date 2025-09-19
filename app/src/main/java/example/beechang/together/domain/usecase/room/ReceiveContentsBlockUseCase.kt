package example.beechang.together.domain.usecase.room

import example.beechang.together.data.response.RoomNotifyContentsBlockResponse
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReceiveContentsBlockUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
) {
    suspend operator fun invoke(): Flow<TogeResult<RoomNotifyContentsBlockResponse>> =
        roomRepository.receiveRoomNotifyContentsBlock()
}