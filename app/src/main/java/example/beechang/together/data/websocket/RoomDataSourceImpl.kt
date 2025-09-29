package example.beechang.together.data.websocket

import example.beechang.together.data.request.ChoiceDuplicateConnectionRequest
import example.beechang.together.data.request.RoomBasicMemberRequest
import example.beechang.together.data.request.RoomChangeCameraRequest
import example.beechang.together.data.request.RoomChangeMicRequest
import example.beechang.together.data.request.RoomCodeRequest
import example.beechang.together.data.request.RoomDecisionWaitingEnterRequest
import example.beechang.together.data.request.RoomMemberRequest
import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.ChoiceDuplicateConnectionResponse
import example.beechang.together.data.response.ConnectionCheckResponse
import example.beechang.together.data.response.RoomCreateResponse
import example.beechang.together.data.response.RoomMemberResponse
import example.beechang.together.data.response.RoomNotifyBasicResponse
import example.beechang.together.data.response.RoomNotifyChangingCameraStatusResponse
import example.beechang.together.data.response.RoomNotifyChangingMicStatusResponse
import example.beechang.together.data.response.RoomNotifyContentsBlockResponse
import example.beechang.together.data.response.RoomNotifyUpdateParticipantResponse
import example.beechang.together.data.response.RoomNotifyWaitResponse
import example.beechang.together.data.response.RoomNotifyWaitingResultResponse
import example.beechang.together.data.response.SocketEventConstants
import example.beechang.together.data.response.handler.socketEventToResultFlow
import example.beechang.together.data.response.handler.togeToResult
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDataSourceImpl @Inject constructor(
    private val webSocketClient: WebSocketClient,
) : RoomDataSource {

    override suspend fun connect(accessToken: String, sessionId: String): TogeResult<Boolean> =
        if (webSocketClient.connect(accessToken, sessionId)) {
            TogeResult.Success(true)
        } else {
            TogeResult.Error(togeError = TogeError.FailedToConnectRoom)
        }

    override suspend fun choiceDuplicateConnection(
        forceDisconnectExisting: Boolean,
        accessToken: String,
        sessionId: String,
    ): TogeResult<ChoiceDuplicateConnectionResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.DUPLICATE_CONNECTION_CHOICE,
                request = ChoiceDuplicateConnectionRequest(
                    forceDisconnect = forceDisconnectExisting,
                    accessToken = accessToken,
                    sessionId = sessionId
                ),
                responseType = ChoiceDuplicateConnectionResponse.serializer()
            )
        }

    override suspend fun disconnect(): TogeResult<Boolean> =
        if (webSocketClient.disconnect()) {
            TogeResult.Success(true)
        } else {
            TogeResult.Error(togeError = TogeError.UnknownErrorFailedToCalling)
        }

    override suspend fun createRoom(roomCode: String): TogeResult<RoomCreateResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.ROOM_CREATE,
                request = RoomCodeRequest(roomCode = roomCode),
                responseType = RoomCreateResponse.serializer()
            )
        }

    override suspend fun requestWaitingEnter(roomCode: String): TogeResult<BaseResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.ROOM_REQUEST_JOIN,
                request = RoomCodeRequest(roomCode = roomCode),
                responseType = BaseResponse.serializer()
            )
        }

    override suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean,
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.ROOM_DECIDE_JOIN_FROM_HOST,
            request = RoomDecisionWaitingEnterRequest(
                roomCode = roomCode,
                userId = targetUserId,
                isApprove = isApprove
            ),
            responseType = BaseResponse.serializer()
        )
    }

    override suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean,
    ): TogeResult<RoomMemberResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.ROOM_MEMBER_LIST,
            request = RoomMemberRequest(
                roomCode = roomCode,
                includingMyself = isIncludingMySelf
            ),
            responseType = RoomMemberResponse.serializer()
        )
    }

    override suspend fun expelMemberFromRoom(
        roomCode: String,
        targetUserId: String,
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.ROOM_MEMBER_EXPEL,
            request = RoomBasicMemberRequest(
                roomCode = roomCode,
                userId = targetUserId
            ),
            responseType = BaseResponse.serializer()
        )
    }

    override suspend fun changeMicStatus(
        roomCode: String,
        isMicrophoneOn: Boolean,
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.CALL_CHANGE_MIC,
            request = RoomChangeMicRequest(
                roomCode = roomCode,
                isMicrophoneOn = isMicrophoneOn
            ),
            responseType = BaseResponse.serializer()
        )
    }

    override suspend fun changeCameraStatus(
        roomCode: String,
        isCameraOn: Boolean,
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.CALL_CHANGE_CAMERA,
            request = RoomChangeCameraRequest(
                roomCode = roomCode,
                isCameraOn = isCameraOn
            ),
            responseType = BaseResponse.serializer()
        )
    }

    override suspend fun receiveConnectionState(): Flow<TogeResult<ConnectionCheckResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.CHECK_CONNECTION,
            resType = ConnectionCheckResponse.serializer()
        )

    override suspend fun receiveForcedLogoutByDuplicateConnection(): Flow<TogeResult<BaseResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.FORCE_LOGOUT_BY_DUPLICATE_CONNECTION,
            resType = BaseResponse.serializer()
        )

    override suspend fun receiveRoomNotifyWaitingList(): Flow<TogeResult<RoomNotifyWaitResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_WAIT,
            resType = RoomNotifyWaitResponse.serializer()
        )

    override suspend fun receiveRoomResultWaiting(): Flow<TogeResult<RoomNotifyWaitingResultResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_DECIDE_JOIN_FROM_HOST,
            resType = RoomNotifyWaitingResultResponse.serializer()
        )

    override suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<RoomNotifyUpdateParticipantResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_UPDATE_PARTICIPANT,
            resType = RoomNotifyUpdateParticipantResponse.serializer()
        )

    override suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomNotifyChangingMicStatusResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.CALL_NOTIFY_CHANGE_MIC,
            resType = RoomNotifyChangingMicStatusResponse.serializer()
        )

    override suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomNotifyChangingCameraStatusResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.CALL_NOTIFY_CHANGE_CAMERA,
            resType = RoomNotifyChangingCameraStatusResponse.serializer()
        )

    override suspend fun receiveRoomNotifyContentsBlock(): Flow<TogeResult<RoomNotifyContentsBlockResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.CALL_CONTENTS_BLOCK,
            resType = RoomNotifyContentsBlockResponse.serializer()
        )

    override suspend fun receiveRoomNotifyBeExpelledFromHost(): Flow<TogeResult<RoomNotifyBasicResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_EXPEL,
            resType = RoomNotifyBasicResponse.serializer()
        )

    override suspend fun receiveRoomConnectionState(): Flow<WebSocketConnectionState> =
        webSocketClient.connectionStateFlow

}

