package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class ReceiveDisconnectedRoomUseCase @Inject constructor(
    private val receiveRoomConnectionStateUseCase: ReceiveRoomConnectionStateUseCase
) {
    suspend operator fun invoke(): Flow<TogeResult<RoomConnectionState>> =
        receiveRoomConnectionStateUseCase()
            .filter { result ->
                result is TogeResult.Success && result.data == RoomConnectionState.FAILED_RECONNECT
            }
}