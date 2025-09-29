package example.beechang.together.domain.usecase.room

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.ConnectionState
import example.beechang.together.domain.repository.RoomRepository
import example.beechang.together.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class ConnectRoomUseCase @Inject constructor(
    private val roomRepository: RoomRepository,
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(): TogeResult<ConnectionState> = coroutineScope {
        val accessToken = userRepository.getAccessToken() ?: ""
        val sessionId = userRepository.getSessionId() ?: ""

        val connectionStateDeferred = async {
            roomRepository.receiveConnectionState().first()
        }

        when (val connectResult = roomRepository.connect(accessToken, sessionId)) {
            is TogeResult.Success -> {
                try {
                    withTimeoutOrNull(10000) {
                        connectionStateDeferred.await()
                    } ?: TogeResult.Error(msg = "ConnectRoomUseCase : receiveConnectionState timeout")
                } catch (e: Exception) {
                    TogeResult.Error(
                        msg = "ConnectRoomUseCase : Exception - ${e.message}",
                        exception = e
                    )
                }
            }

            is TogeResult.Error -> {
                connectionStateDeferred.cancel()
                connectResult
            }
        }
    }
}