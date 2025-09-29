package example.beechang.together.domain.repository

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomUserInteraction
import example.beechang.together.domain.model.RoomCode
import example.beechang.together.domain.model.RoomConnectionState
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.model.RoomParticipantInfo
import example.beechang.together.domain.model.RoomWaitingMembers
import example.beechang.together.domain.model.ConnectionState
import example.beechang.together.domain.model.UpdatedRoomParticipant
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun connect(accessToken: String, sessionId: String): TogeResult<Boolean>

    suspend fun choiceDuplicateConnection(
        forceDisconnectExisting: Boolean,
        accessToken: String,
        sessionId: String,
    ): TogeResult<Boolean>

    suspend fun disconnect(): TogeResult<Boolean>
    suspend fun createRoom(roodCode: String? = null): TogeResult<RoomCode>
    suspend fun requestWaitingEnter(roomCode: String): TogeResult<Boolean>
    suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean,
    ): TogeResult<Boolean>

    suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean,
    ): TogeResult<RoomParticipantInfo>

    suspend fun expelMemberFromRoom(roomCode: String, targetUserId: String): TogeResult<Boolean>

    suspend fun changeMicStatus(roomCode: String, isMicrophoneOn: Boolean): TogeResult<Boolean>
    suspend fun changeCameraStatus(roomCode: String, isCameraOn: Boolean): TogeResult<Boolean>

    suspend fun receiveConnectionState(): Flow<TogeResult<ConnectionState>>
    suspend fun receiveForcedLogoutByDuplicateConnection(): Flow<TogeResult<Boolean>>

    suspend fun receiveRoomNotifyWait(): Flow<TogeResult<RoomWaitingMembers>>
    suspend fun receiveRoomNotifyWaitingResult(): Flow<TogeResult<Boolean>>
    suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<UpdatedRoomParticipant>>
    suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomParticipant>>
    suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomParticipant>>
    suspend fun receiveRoomNotifyContentsBlock(): Flow<TogeResult<RoomUserInteraction>>
    suspend fun receiveRoomNotifyBeExpelled(): Flow<TogeResult<Boolean>>
    suspend fun receiveRoomConnectionState(): Flow<RoomConnectionState>
}