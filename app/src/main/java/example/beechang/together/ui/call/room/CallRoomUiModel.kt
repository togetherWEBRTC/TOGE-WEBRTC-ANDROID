package example.beechang.together.ui.call.room

import android.os.Parcelable
import example.beechang.together.BuildConfig
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.model.UserInfo
import kotlinx.parcelize.Parcelize


@Parcelize
data class RoomParticipantUi(
    val userId: String = "",
    val name: String = "",
    val profileUrl: String = "",
    val isOwner: Boolean = false,
    val isMicrophoneOn: Boolean = true,
    val isCameraOn: Boolean = true,
    val isHandRaised: Boolean = false
) : Parcelable {
    fun getProfileFullUrl(): String {
        return BuildConfig.RES_URL + profileUrl
    }
}

enum class CallLayoutType {
    SINGLE,
    FLOATING,
    GRID
}

enum class VideoScaleType {
    ASPECT_FIT,
    ASPECT_FILL,
    ASPECT_BALANCED
}

fun RoomParticipant.toUi(): RoomParticipantUi {
    return RoomParticipantUi(
        userId = userId,
        name = name,
        profileUrl = profileUrl,
        isOwner = isOwner,
        isMicrophoneOn = isMicrophoneOn,
        isCameraOn = isCameraOn,
        isHandRaised = isHandRaised
    )
}

fun UserInfo.toRoomParticipantUi(): RoomParticipantUi {
    return RoomParticipantUi(
        userId = userId,
        name = nickname,
        profileUrl = profileImageUrl
    )
}

