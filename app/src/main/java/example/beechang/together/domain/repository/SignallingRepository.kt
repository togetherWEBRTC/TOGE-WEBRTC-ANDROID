package example.beechang.together.domain.repository

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomIceCandidate
import example.beechang.together.domain.model.RoomSdp
import example.beechang.together.domain.model.RoomUserId
import kotlinx.coroutines.flow.Flow

interface SignallingRepository {
    suspend fun sendRtcReady(roomCode: String): TogeResult<Boolean>
    suspend fun sendOffer(roomCode: String, toUserId: String, sdp: String): TogeResult<Boolean>
    suspend fun sendAnswer(roomCode: String, toUserId: String, sdp: String): TogeResult<Boolean>
    suspend fun sendIceCandidate(
        roomCode: String,
        toUserId: String,
        candidate: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ): TogeResult<Boolean>

    suspend fun receiveRtcReady(): Flow<TogeResult<RoomUserId>>
    suspend fun receiveOffer(): Flow<TogeResult<RoomSdp>>
    suspend fun receiveAnswer(): Flow<TogeResult<RoomSdp>>
    suspend fun receiveIceCandidate(): Flow<TogeResult<RoomIceCandidate>>
}