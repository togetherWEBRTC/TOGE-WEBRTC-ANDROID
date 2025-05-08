package example.beechang.together.domain.repository

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomCode
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.model.RoomWaitingMembers
import example.beechang.together.domain.model.UpdatedRoomParticipant
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    suspend fun connect(): TogeResult<Boolean>
    suspend fun disconnect(): TogeResult<Boolean>
    suspend fun createRoom(roodCode: String? = null): TogeResult<RoomCode>
    suspend fun requestWaitingEnter(roomCode: String): TogeResult<Boolean>
    suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean
    ): TogeResult<Boolean>

    suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean
    ): TogeResult<List<RoomParticipant>>

    suspend fun changeMicStatus(roomCode: String, isMicrophoneOn: Boolean): TogeResult<Boolean>
    suspend fun changeCameraStatus(roomCode: String, isCameraOn: Boolean): TogeResult<Boolean>

    suspend fun receiveRoomNotifyWait(): Flow<TogeResult<RoomWaitingMembers>>
    suspend fun receiveRoomNotifyWaitingResult(): Flow<TogeResult<Boolean>>
    suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<UpdatedRoomParticipant>>
    suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomParticipant>>
    suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomParticipant>>
}