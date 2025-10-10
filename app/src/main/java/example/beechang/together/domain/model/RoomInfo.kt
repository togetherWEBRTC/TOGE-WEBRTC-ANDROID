package example.beechang.together.domain.model


data class RoomParticipant(
    val userId: String = "",
    val name: String = "",
    val profileUrl: String = "",
    val isOwner: Boolean = false,
    val isMicrophoneOn: Boolean = false,
    val isCameraOn: Boolean = false,
    val isHandRaised: Boolean = false,
)

data class RoomWaitingMembers(
    val waitingList: List<UserInfo> = emptyList(),
    val updatedUser: UserInfo,
    val isAdded: Boolean = false,
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
    val isJoined: Boolean = false,
    val joinedUserInteractionForMe: RoomUserInteraction? = null,
)


data class RoomUserInteraction(
    val targetUserId: String,
    val isContentBlocked: Boolean = false,
    val isShowBlockIndicator: Boolean = false,
)

data class RoomParticipantInfo(
    val participants: List<RoomParticipant>,
    val userInteractions: List<RoomUserInteraction>,
)

enum class RoomConnectionState {
    CONNECTED,
    DISCONNECTED,
    RECONNECTING,
    RECONNECTED,
    FAILED_RECONNECT,
    PENDING
}

enum class SocketConnectionStatus {
    NEW_CONNECTION,
    RECONNECTION_SUCCESS,
    DUPLICATE_CONNECTION;

    companion object {
        fun fromString(status: String?): SocketConnectionStatus {
            return entries.find { it.name == status } ?: NEW_CONNECTION
        }
    }
}

enum class SocketUserState {
    IDLE,
    IN_ROOM,
    WAITING_FOR_ROOM;

    companion object {
        fun fromString(state: String?): SocketUserState {
            return entries.find { it.name == state } ?: IDLE
        }
    }
}

data class ConnectionState(
    val name: String,
    val connectionStatus: SocketConnectionStatus,
    val userState: SocketUserState,
    val message: String,
    val isDuplicateConnection: Boolean = false,
    val existingSocketId: String? = null,
    val currentSocketId: String? = null,
)