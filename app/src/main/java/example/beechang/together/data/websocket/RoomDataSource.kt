package example.beechang.together.data.websocket

import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.RoomCreateResponse
import example.beechang.together.data.response.RoomMemberResponse
import example.beechang.together.data.response.RoomNotifyChangingCameraStatusResponse
import example.beechang.together.data.response.RoomNotifyChangingMicStatusResponse
import example.beechang.together.data.response.RoomNotifyUpdateParticipantResponse
import example.beechang.together.data.response.RoomNotifyWaitResponse
import example.beechang.together.data.response.RoomNotifyWaitingResultResponse
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.flow.Flow

interface RoomDataSource {
    suspend fun connect(accessToken: String): TogeResult<Boolean>
    suspend fun disconnect(): TogeResult<Boolean>
    suspend fun createRoom(roomCode: String): TogeResult<RoomCreateResponse>
    suspend fun requestWaitingEnter(roomCode: String): TogeResult<BaseResponse>
    suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean
    ): TogeResult<BaseResponse>

    suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean
    ): TogeResult<RoomMemberResponse>

    suspend fun changeMicStatus(roomCode: String, isMicrophoneOn: Boolean): TogeResult<BaseResponse>
    suspend fun changeCameraStatus(roomCode: String, isCameraOn: Boolean): TogeResult<BaseResponse>

    suspend fun receiveRoomNotifyWaitingList(): Flow<TogeResult<RoomNotifyWaitResponse>>
    suspend fun receiveRoomResultWaiting(): Flow<TogeResult<RoomNotifyWaitingResultResponse>>
    suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<RoomNotifyUpdateParticipantResponse>>
    suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomNotifyChangingMicStatusResponse>>
    suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomNotifyChangingCameraStatusResponse>>
}