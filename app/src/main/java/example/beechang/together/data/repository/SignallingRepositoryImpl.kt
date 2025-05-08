package example.beechang.together.data.repository

import example.beechang.together.data.websocket.RoomDataSource
import example.beechang.together.data.websocket.SignallingDataSource
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.data.map
import example.beechang.together.domain.data.mapToge
import example.beechang.together.domain.model.RoomIceCandidate
import example.beechang.together.domain.model.RoomSdp
import example.beechang.together.domain.model.RoomUserId
import example.beechang.together.domain.repository.SignallingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignallingRepositoryImpl @Inject constructor(
    private val roomDataSource: RoomDataSource,
    private val signallingDataSource: SignallingDataSource,
) : SignallingRepository {
    override suspend fun sendRtcReady(roomCode: String): TogeResult<Boolean> =
        signallingDataSource.sendRtcReady(roomCode = roomCode)
            .map { it.toSuccessBoolean() }

    override suspend fun sendOffer(
        roomCode: String,
        toUserId: String,
        sdp: String
    ): TogeResult<Boolean> =
        signallingDataSource.sendOffer(
            roomCode = roomCode,
            toUserId = toUserId,
            sdp = sdp
        ).map { it.toSuccessBoolean() }

    override suspend fun sendAnswer(
        roomCode: String,
        toUserId: String,
        sdp: String
    ): TogeResult<Boolean> =
        signallingDataSource.sendAnswer(
            roomCode = roomCode,
            toUserId = toUserId,
            sdp = sdp
        ).map { it.toSuccessBoolean() }

    override suspend fun sendIceCandidate(
        roomCode: String,
        toUserId: String,
        candidate: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ): TogeResult<Boolean> =
        signallingDataSource.sendIceCandidate(
            roomCode = roomCode,
            toUserId = toUserId,
            candidate = candidate,
            sdpMid = sdpMid,
            sdpMLineIndex = sdpMLineIndex
        ).map { it.toSuccessBoolean() }

    override suspend fun receiveRtcReady(): Flow<TogeResult<RoomUserId>> =
        signallingDataSource.receiveRtcReady()
            .mapToge { it.toUserId() }

    override suspend fun receiveOffer(): Flow<TogeResult<RoomSdp>> =
        signallingDataSource.receiveOffer()
            .mapToge { it.toRoomSdp() }

    override suspend fun receiveAnswer(): Flow<TogeResult<RoomSdp>> =
        signallingDataSource.receiveAnswer()
            .mapToge { it.toRoomSdp() }

    override suspend fun receiveIceCandidate(): Flow<TogeResult<RoomIceCandidate>> =
        signallingDataSource.receiveIceCandidate()
            .mapToge { it.toRoomIceCandiate() }
}