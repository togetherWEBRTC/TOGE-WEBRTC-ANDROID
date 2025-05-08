package example.beechang.together.data.response

import example.beechang.together.domain.model.RoomCode
import example.beechang.together.domain.model.RoomIceCandidate
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.model.RoomSdp
import example.beechang.together.domain.model.RoomUserId
import example.beechang.together.domain.model.RoomWaitingMembers
import example.beechang.together.domain.model.UpdatedRoomParticipant
import example.beechang.together.domain.model.UserInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class RoomCreateResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("roomCode") val roomCode: String
) : TogeResponse {
    fun toRoomCode() = RoomCode(
        roomCode = roomCode
    )
}

@Serializable
data class RoomMemberResponse(
    @SerialName("code") override val code: Int,
    @SerialName("message") override val message: String,
    @SerialName("roomMemberList") val participants: List<RoomParticipantResponse>,
) : TogeResponse {
    fun toRoomParticipant() = participants.map { it.toRoomParticipant() }
}

@Serializable
data class RoomSimpleUserResponse(
    @SerialName("userId") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("profileUrl") val profileUrl: String
) {
    fun toUserInfo() = UserInfo(
        userId = userId,
        nickname = name,
        profileImageUrl = profileUrl
    )
}

@Serializable
data class RoomUserIdResponse(
    @SerialName("userId") val userId: String,
    @SerialName("name") val name: String = "",
) {
    fun toUserId() = RoomUserId(userId = userId)
}

@Serializable
data class RoomSdpResponse(
    @SerialName("sdp") val sdp: String,
    @SerialName("fromUserId") val fromUserId: String
) {
    fun toRoomSdp() = RoomSdp(
        sdp = sdp,
        fromUserId = fromUserId
    )
}

@Serializable
data class RoomIceCandidateResponse(
    @SerialName("candidate") val candidate: String,
    @SerialName("sdpMid") val sdpMid: String,
    @SerialName("sdpMLineIndex") val sdpMLineIndex: Int,
    @SerialName("fromUserId") val fromUserId: String
) {
    fun toRoomIceCandiate() = RoomIceCandidate(
        candidate = candidate,
        sdpMid = sdpMid,
        sdpMLineIndex = sdpMLineIndex,
        fromUserId = fromUserId
    )
}

@Serializable
data class RoomNotifyWaitResponse(
    @SerialName("name") val name: String,
    @SerialName("waitingList") val waitingList: List<RoomSimpleUserResponse>,
    @SerialName("updatedUser") val updatedUser: RoomSimpleUserResponse,
    @SerialName("isAdded") val isAdded: Boolean
) {
    fun toRoomWaitingMembers() = RoomWaitingMembers(
        waitingList = waitingList.map { it.toUserInfo() },
        updatedUser = updatedUser.toUserInfo(),
        isAdded = isAdded
    )
}

@Serializable
data class RoomNotifyWaitingResultResponse(
    @SerialName("name") val name: String,
    @SerialName("isApprove") val isApprove: Boolean
)

@Serializable
data class RoomParticipantResponse(
    @SerialName("userId") val userId: String,
    @SerialName("name") val name: String,
    @SerialName("profileUrl") val profileUrl: String,
    @SerialName("isOwner") val isOwner: Boolean,
    @SerialName("isMicrophoneOn") val isMicrophoneOn: Boolean,
    @SerialName("isCameraOn") val isCameraOn: Boolean,
    @SerialName("isHandRaised") val isHandRaised: Boolean
) {
    fun toRoomParticipant() = RoomParticipant(
        userId = userId,
        name = name,
        profileUrl = profileUrl,
        isOwner = isOwner,
        isMicrophoneOn = isMicrophoneOn,
        isCameraOn = isCameraOn,
        isHandRaised = isHandRaised
    )
}

@Serializable
data class RoomNotifyUpdateParticipantResponse(
    @SerialName("name") val name: String,
    @SerialName("participants") val participants: List<RoomParticipantResponse>,
    @SerialName("isJoined") val isJoined: Boolean,
    @SerialName("changedUser") val changedUser: RoomSimpleUserResponse
) {
    fun toUpdatedRoomParticipant() = UpdatedRoomParticipant(
        participants = participants.map { it.toRoomParticipant() },
        updatedUser = changedUser.toUserInfo(),
        isJoined = isJoined
    )
}

@Serializable
data class RoomNotifyChangingMicStatusResponse(
    @SerialName("name") val name: String,
    @SerialName("changedUserInfo") val participants: RoomParticipantResponse,
    @SerialName("isMicrophoneOn") val isMicOn: Boolean,
) {
    fun toParticipant() = RoomParticipant(
        userId = participants.userId,
        name = participants.name,
        profileUrl = participants.profileUrl,
        isOwner = participants.isOwner,
        isMicrophoneOn = isMicOn,
        isCameraOn = participants.isCameraOn,
        isHandRaised = participants.isHandRaised
    )
}

@Serializable
data class RoomNotifyChangingCameraStatusResponse(
    @SerialName("name") val name: String,
    @SerialName("changedUserInfo") val participants: RoomParticipantResponse,
    @SerialName("isCameraOn") val isCameraOn: Boolean,
) {
    fun toParticipant() = RoomParticipant(
        userId = participants.userId,
        name = participants.name,
        profileUrl = participants.profileUrl,
        isOwner = participants.isOwner,
        isMicrophoneOn = participants.isMicrophoneOn,
        isCameraOn = isCameraOn,
        isHandRaised = participants.isHandRaised
    )
}