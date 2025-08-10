package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.model.RoomConnectionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import javax.inject.Inject

class ReceiveDisconnectedRoomUseCase @Inject constructor(
    private val receiveRoomConnectionStateUseCase: ReceiveRoomConnectionStateUseCase
) {
    suspend operator fun invoke(): Flow<RoomConnectionState> =
        receiveRoomConnectionStateUseCase()
            .filter { result ->
                result == RoomConnectionState.FAILED_RECONNECT
            }
}