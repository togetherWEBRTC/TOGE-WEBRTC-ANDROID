package example.beechang.together.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomCodeRequest(
    @SerialName("roomCode") val roomCode: String = "",
)

@Serializable
data class RoomSendSdpRequest(
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("toUserId") val toUserId: String = "",
    @SerialName("sdp") val sdp: String = "",
)

@Serializable
data class RoomSendIceRequest(
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("toUserId") val toUserId: String = "",
    @SerialName("candidate") val candidate: String = "",
    @SerialName("sdpMid") val sdpMid: String = "",
    @SerialName("sdpMLineIndex") val sdpMLineIndex: Int = 0,
)

@Serializable
data class RoomMemberRequest(
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("includingMyself") val includingMyself: Boolean = false,
)

@Serializable
data class RoomDecisionWaitingEnterRequest(
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("userId") val userId: String = "",
    @SerialName("isApprove") val isApprove: Boolean = false,
)

@Serializable
data class RoomChangeMicRequest(
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("isMicrophoneOn") val isMicrophoneOn: Boolean = false,
)

@Serializable
data class RoomChangeCameraRequest(
    @SerialName("roomCode") val roomCode: String = "",
    @SerialName("isCameraOn") val isCameraOn: Boolean = false,
)