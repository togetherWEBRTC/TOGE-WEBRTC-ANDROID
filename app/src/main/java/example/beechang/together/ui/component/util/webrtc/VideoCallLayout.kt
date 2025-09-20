package example.beechang.together.ui.component.util.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import example.beechang.together.ui.call.room.CallLayoutType
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.RoomUserInteractionUi
import example.beechang.together.webrtc.WebRtcData
import org.webrtc.EglBase
import kotlin.collections.toList

@Composable
fun VideoCallLayout(
    myUserId: String,
    participants: LinkedHashMap<String, RoomParticipantUi>,
    webRtcData: Map<String, WebRtcData>,
    userInteractions: Map<String, RoomUserInteractionUi> = emptyMap(),
    layoutType: CallLayoutType,
    eglBase: EglBase?,
    onParticipantSwap: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (participants.isEmpty()) return

    when (layoutType) {
        CallLayoutType.SINGLE -> {
            val firstParticipant = participants.values.firstOrNull() ?: return
            val participantWebRtcData = webRtcData[firstParticipant.userId] ?: WebRtcData()

            SingleParticipantLayout(
                participant = firstParticipant,
                webRtcData = participantWebRtcData,
                eglBase = eglBase,
                userInteraction = userInteractions[firstParticipant.userId],
                modifier = modifier
            )
        }

        CallLayoutType.FLOATING -> {
            TwoParticipantsFloatingLayout(
                localUserId = myUserId,
                participants = participants,
                eglBase = eglBase,
                webRtcData = webRtcData,
                userInteractions = userInteractions,
                onSwapParticipants = onParticipantSwap,
                modifier = modifier
            )
        }

        CallLayoutType.GRID -> {
            val participantsList = participants.values.toList()

            GridParticipantsLayout(
                participants = participantsList,
                eglBase = eglBase,
                webRtcData = webRtcData,
                userInteractions = userInteractions,
                onParticipantSelected = { userId ->
                    val otherUserId = participants.keys.firstOrNull { it != userId }
                    if (otherUserId != null) {
                        onParticipantSwap(userId, otherUserId)
                    }
                },
                modifier = modifier
            )
        }
    }
}