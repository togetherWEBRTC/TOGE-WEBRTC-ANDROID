package example.beechang.together.data.websocket

import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.RoomIceCandidateResponse
import example.beechang.together.data.response.RoomSdpResponse
import example.beechang.together.data.response.RoomUserIdResponse
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.flow.Flow

interface SignallingDataSource {
    suspend fun sendRtcReady(roomCode: String): TogeResult<BaseResponse>
    suspend fun sendOffer(roomCode: String, toUserId: String, sdp: String): TogeResult<BaseResponse>
    suspend fun sendAnswer(
        roomCode: String,
        toUserId: String,
        sdp: String
    ): TogeResult<BaseResponse>

    suspend fun sendIceCandidate(
        roomCode: String,
        toUserId: String,
        candidate: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ): TogeResult<BaseResponse>

    suspend fun receiveRtcReady(): Flow<TogeResult<RoomUserIdResponse>>
    suspend fun receiveOffer(): Flow<TogeResult<RoomSdpResponse>>
    suspend fun receiveAnswer(): Flow<TogeResult<RoomSdpResponse>>
    suspend fun receiveIceCandidate(): Flow<TogeResult<RoomIceCandidateResponse>>
}