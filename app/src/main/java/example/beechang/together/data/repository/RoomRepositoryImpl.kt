package example.beechang.together.data.repository

import example.beechang.together.data.response.handler.mapSuccessOrProvideError
import example.beechang.together.data.websocket.RoomDataSource
import example.beechang.together.data.websocket.WebSocketConnectionState
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.data.map
import example.beechang.together.domain.data.mapToge
import example.beechang.together.domain.model.RoomUserInteraction
import example.beechang.together.domain.model.RoomCode
import example.beechang.together.domain.model.RoomConnectionState
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.model.RoomParticipantInfo
import example.beechang.together.domain.model.RoomWaitingMembers
import example.beechang.together.domain.model.ConnectionState
import example.beechang.together.domain.model.SocketConnectionStatus
import example.beechang.together.domain.model.SocketUserState
import example.beechang.together.domain.model.UpdatedRoomParticipant
import example.beechang.together.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val roomDataSource: RoomDataSource,
) : RoomRepository {

    override suspend fun connect(accessToken: String, sessionId: String): TogeResult<Boolean> =
        roomDataSource.connect(accessToken, sessionId)

    override suspend fun choiceDuplicateConnection(
        forceDisconnectExisting: Boolean,
        accessToken: String,
        sessionId: String,
    ): TogeResult<Boolean> =
        roomDataSource.choiceDuplicateConnection(forceDisconnectExisting, accessToken, sessionId)
            .map {
                it.isAllowed ?: false
            }

    override suspend fun disconnect(): TogeResult<Boolean> =
        roomDataSource.disconnect()

    override suspend fun createRoom(roodCode: String?): TogeResult<RoomCode> =
        roomDataSource.createRoom(roomCode = roodCode ?: "")
            .mapSuccessOrProvideError(errorType = TogeError.FailedToCreateRoom) {
                it.toRoomCode()
            }

    override suspend fun requestWaitingEnter(roomCode: String): TogeResult<Boolean> =
        roomDataSource.requestWaitingEnter(roomCode = roomCode).map { it.toSuccessBoolean() }

    override suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean,
    ): TogeResult<Boolean> =
        roomDataSource.requestDecisionWaitingEnter(
            roomCode = roomCode,
            targetUserId = targetUserId,
            isApprove = isApprove
        ).map { it.toSuccessBoolean() }

    override suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean,
    ): TogeResult<RoomParticipantInfo> =
        roomDataSource.getRoomParticipant(
            roomCode = roomCode,
            isIncludingMySelf = isIncludingMySelf
        ).map {
            RoomParticipantInfo(
                participants = it.toRoomParticipant(),
                userInteractions = it.toRoomUserInteractions()
            )
        }

    override suspend fun expelMemberFromRoom(
        roomCode: String,
        targetUserId: String,
    ): TogeResult<Boolean> =
        roomDataSource.expelMemberFromRoom(roomCode = roomCode, targetUserId = targetUserId)
            .map { it.toSuccessBoolean() }

    override suspend fun changeMicStatus(
        roomCode: String,
        isMicrophoneOn: Boolean,
    ): TogeResult<Boolean> =
        roomDataSource.changeMicStatus(
            roomCode = roomCode,
            isMicrophoneOn = isMicrophoneOn
        ).map { it.toSuccessBoolean() }

    override suspend fun changeCameraStatus(
        roomCode: String,
        isCameraOn: Boolean,
    ): TogeResult<Boolean> =
        roomDataSource.changeCameraStatus(
            roomCode = roomCode,
            isCameraOn = isCameraOn
        ).map { it.toSuccessBoolean() }

    override suspend fun receiveConnectionState(): Flow<TogeResult<ConnectionState>> {
        return roomDataSource.receiveConnectionState().mapToge {
            ConnectionState(
                name = it.name,
                connectionStatus = when (it.connectionStatus) {
                    "NEW_CONNECTION" -> SocketConnectionStatus.NEW_CONNECTION
                    "RECONNECTION_SUCCESS" -> SocketConnectionStatus.RECONNECTION_SUCCESS
                    "DUPLICATE_CONNECTION" -> SocketConnectionStatus.DUPLICATE_CONNECTION
                    else -> SocketConnectionStatus.NEW_CONNECTION
                },
                userState = when (it.userState) {
                    "IDLE" -> SocketUserState.IDLE
                    "IN_ROOM" -> SocketUserState.IN_ROOM
                    "WAITING_FOR_ROOM" -> SocketUserState.WAITING_FOR_ROOM
                    else -> SocketUserState.IDLE
                },
                message = it.message,
                isDuplicateConnection = it.isDuplicateConnection ?: false,
                existingSocketId = it.existingSocketId,
                currentSocketId = it.currentSocketId
            )
        }
    }

    override suspend fun receiveForcedLogoutByDuplicateConnection(): Flow<TogeResult<Boolean>> {
        return roomDataSource.receiveForcedLogoutByDuplicateConnection()
            .mapToge { true }
    }

    override suspend fun receiveRoomNotifyWait(): Flow<TogeResult<RoomWaitingMembers>> {
        return roomDataSource.receiveRoomNotifyWaitingList()
            .mapToge { it.toRoomWaitingMembers() }
    }

    override suspend fun receiveRoomNotifyWaitingResult(): Flow<TogeResult<Boolean>> {
        return roomDataSource.receiveRoomResultWaiting()
            .mapToge { it.isApprove }
    }

    override suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<UpdatedRoomParticipant>> {
        return roomDataSource.receiveRoomUpdatingParticipant()
            .mapToge { it.toUpdatedRoomParticipant() }
    }

    override suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomParticipant>> {
        return roomDataSource.receiveRoomNotifyMicStatus()
            .mapToge { it.toParticipant() }
    }

    override suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomParticipant>> {
        return roomDataSource.receiveRoomNotifyCameraStatus()
            .mapToge { it.toParticipant() }
    }

    override suspend fun receiveRoomNotifyContentsBlock(): Flow<TogeResult<RoomUserInteraction>> {
        return roomDataSource.receiveRoomNotifyContentsBlock()
            .mapToge { it.toRoomUserInteraction() }
    }

    override suspend fun receiveRoomNotifyBeExpelled(): Flow<TogeResult<Boolean>> {
        return roomDataSource.receiveRoomNotifyBeExpelledFromHost()
            .mapToge { true }
    }

    override suspend fun receiveRoomConnectionState(): Flow<RoomConnectionState> =
        roomDataSource.receiveRoomConnectionState().map {
            when (it) {
                WebSocketConnectionState.CONNECTED -> RoomConnectionState.CONNECTED
                WebSocketConnectionState.DISCONNECTED -> RoomConnectionState.DISCONNECTED
                WebSocketConnectionState.RECONNECTING -> RoomConnectionState.RECONNECTING
                WebSocketConnectionState.RECONNECTED -> RoomConnectionState.RECONNECTED
                WebSocketConnectionState.FAILED_RECONNECT -> RoomConnectionState.FAILED_RECONNECT
                WebSocketConnectionState.PENDING -> RoomConnectionState.PENDING
            }
        }

}