package example.beechang.together.data.websocket

import example.beechang.together.data.request.RoomCodeRequest
import example.beechang.together.data.request.RoomSendIceRequest
import example.beechang.together.data.request.RoomSendSdpRequest
import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.RoomIceCandidateResponse
import example.beechang.together.data.response.RoomSdpResponse
import example.beechang.together.data.response.RoomUserIdResponse
import example.beechang.together.data.response.SocketEventConstants
import example.beechang.together.data.response.handler.socketEventToResultFlow
import example.beechang.together.data.response.handler.togeToResult
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignallingDataSourceImpl @Inject constructor(
    private val webSocketClient: WebSocketClient
) : SignallingDataSource {

    override suspend fun sendRtcReady(roomCode: String): TogeResult<BaseResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.RTC_READY,
                request = RoomCodeRequest(roomCode = roomCode),
                responseType = BaseResponse::class
            )
        }

    override suspend fun sendOffer(
        roomCode: String,
        toUserId: String,
        sdp: String
    ): TogeResult<BaseResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.SIGNAL_SEND_OFFER,
                request = RoomSendSdpRequest(roomCode = roomCode, toUserId = toUserId, sdp = sdp),
                responseType = BaseResponse::class
            )
        }

    override suspend fun sendAnswer(
        roomCode: String,
        toUserId: String,
        sdp: String
    ): TogeResult<BaseResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.SIGNAL_SEND_ANSWER,
                request = RoomSendSdpRequest(roomCode = roomCode, toUserId = toUserId, sdp = sdp),
                responseType = BaseResponse::class
            )
        }

    override suspend fun sendIceCandidate(
        roomCode: String,
        toUserId: String,
        candidate: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ): TogeResult<BaseResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.SIGNAL_SEND_ICE,
                request = RoomSendIceRequest(
                    roomCode = roomCode,
                    toUserId = toUserId,
                    candidate = candidate,
                    sdpMid = sdpMid,
                    sdpMLineIndex = sdpMLineIndex
                ),
                responseType = BaseResponse::class
            )
        }

    override suspend fun receiveRtcReady(): Flow<TogeResult<RoomUserIdResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.RTC_READY,
            resType = RoomUserIdResponse::class
        )

    override suspend fun receiveOffer(): Flow<TogeResult<RoomSdpResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.SIGNAL_NOTIFY_OFFER,
            resType = RoomSdpResponse::class
        )

    override suspend fun receiveAnswer(): Flow<TogeResult<RoomSdpResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.SIGNAL_NOTIFY_ANSWER,
            resType = RoomSdpResponse::class
        )

    override suspend fun receiveIceCandidate(): Flow<TogeResult<RoomIceCandidateResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.SIGNAL_NOTIFY_ICE,
            resType = RoomIceCandidateResponse::class
        )
}