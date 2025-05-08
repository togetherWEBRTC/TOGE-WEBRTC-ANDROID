package example.beechang.together.data.websocket

import example.beechang.together.data.request.RoomChangeCameraRequest
import example.beechang.together.data.request.RoomChangeMicRequest
import example.beechang.together.data.request.RoomCodeRequest
import example.beechang.together.data.request.RoomDecisionWaitingEnterRequest
import example.beechang.together.data.request.RoomMemberRequest
import example.beechang.together.data.response.BaseResponse
import example.beechang.together.data.response.RoomCreateResponse
import example.beechang.together.data.response.RoomMemberResponse
import example.beechang.together.data.response.RoomNotifyChangingCameraStatusResponse
import example.beechang.together.data.response.RoomNotifyChangingMicStatusResponse
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
    private val webSocketClient: WebSocketClient
) : RoomDataSource {

    override suspend fun connect(accessToken: String): TogeResult<Boolean> =
        if (webSocketClient.connect(accessToken)) {
            TogeResult.Success(true)
        } else {
            TogeResult.Error(togeError = TogeError.FailedToConnectRoom)
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
                responseType = RoomCreateResponse::class
            )
        }

    override suspend fun requestWaitingEnter(roomCode: String): TogeResult<BaseResponse> =
        togeToResult {
            webSocketClient.emitWithAck(
                event = SocketEventConstants.ROOM_REQUEST_JOIN,
                request = RoomCodeRequest(roomCode = roomCode),
                responseType = BaseResponse::class
            )
        }

    override suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.ROOM_DECIDE_JOIN_FROM_HOST,
            request = RoomDecisionWaitingEnterRequest(
                roomCode = roomCode,
                userId = targetUserId,
                isApprove = isApprove
            ),
            responseType = BaseResponse::class
        )
    }

    override suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean
    ): TogeResult<RoomMemberResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.ROOM_MEMBER_LIST,
            request = RoomMemberRequest(
                roomCode = roomCode,
                includingMyself = isIncludingMySelf
            ),
            responseType = RoomMemberResponse::class
        )
    }

    override suspend fun changeMicStatus(
        roomCode: String,
        isMicrophoneOn: Boolean
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.CALL_CHANGE_MIC,
            request = RoomChangeMicRequest(
                roomCode = roomCode,
                isMicrophoneOn = isMicrophoneOn
            ),
            responseType = BaseResponse::class
        )
    }

    override suspend fun changeCameraStatus(
        roomCode: String,
        isCameraOn: Boolean
    ): TogeResult<BaseResponse> = togeToResult {
        webSocketClient.emitWithAck(
            event = SocketEventConstants.CALL_CHANGE_CAMERA,
            request = RoomChangeCameraRequest(
                roomCode = roomCode,
                isCameraOn = isCameraOn
            ),
            responseType = BaseResponse::class
        )
    }

    override suspend fun receiveRoomNotifyWaitingList(): Flow<TogeResult<RoomNotifyWaitResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_WAIT,
            resType = RoomNotifyWaitResponse::class
        )

    override suspend fun receiveRoomResultWaiting(): Flow<TogeResult<RoomNotifyWaitingResultResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_DECIDE_JOIN_FROM_HOST,
            resType = RoomNotifyWaitingResultResponse::class
        )

    override suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<RoomNotifyUpdateParticipantResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.ROOM_NOTIFY_UPDATE_PARTICIPANT,
            resType = RoomNotifyUpdateParticipantResponse::class
        )

    override suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomNotifyChangingMicStatusResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.CALL_NOTIFY_CHANGE_MIC,
            resType = RoomNotifyChangingMicStatusResponse::class
        )

    override suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomNotifyChangingCameraStatusResponse>> =
        socketEventToResultFlow(
            webSocketClient = webSocketClient,
            eventName = SocketEventConstants.CALL_NOTIFY_CHANGE_CAMERA,
            resType = RoomNotifyChangingCameraStatusResponse::class
        )

}

