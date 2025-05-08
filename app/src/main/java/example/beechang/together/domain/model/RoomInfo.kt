package example.beechang.together.domain.model


data class RoomParticipant(
    val userId: String = "",
    val name: String = "",
    val profileUrl: String = "",
    val isOwner: Boolean = false,
    val isMicrophoneOn: Boolean = false,
    val isCameraOn: Boolean = false,
    val isHandRaised: Boolean = false
)

data class RoomWaitingMembers(
    val waitingList: List<UserInfo> = emptyList(),
    val updatedUser: UserInfo,
    val isAdded: Boolean = false
)

data class RoomCode(
    val roomCode: String = "",
)

data class RoomUserId(
    val userId: String = "",
)

data class RoomSdp(
    val sdp: String = "",
    val fromUserId: String = "",
)

data class RoomIceCandidate(
    val candidate: String = "",
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val fromUserId: String = "",
)

data class UpdatedRoomParticipant(
    val participants: List<RoomParticipant> = emptyList(),
    val updatedUser: UserInfo,
    val isJoined: Boolean = false
)