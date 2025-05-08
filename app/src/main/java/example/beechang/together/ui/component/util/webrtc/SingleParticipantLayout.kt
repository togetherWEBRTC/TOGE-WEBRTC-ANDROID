package example.beechang.together.ui.component.util.webrtc

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.VideoScaleType
import example.beechang.together.webrtc.WebRtcData
import org.webrtc.EglBase

@Composable
fun SingleParticipantLayout(
    participant: RoomParticipantUi,
    webRtcData: WebRtcData,
    eglBase: EglBase?,
    modifier: Modifier = Modifier
) {
    ParticipantCallingView(
        participant = participant,
        webRtcData = webRtcData,
        eglBase = eglBase,
        scaleType = VideoScaleType.ASPECT_FILL,
        modifier = modifier
    )
}