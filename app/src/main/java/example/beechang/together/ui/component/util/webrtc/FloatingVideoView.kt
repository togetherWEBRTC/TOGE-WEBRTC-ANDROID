package example.beechang.together.ui.component.util.webrtc

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.VideoScaleType
import example.beechang.together.webrtc.WebRtcData
import org.webrtc.EglBase

@Composable
fun FloatingVideoView(
    participant: RoomParticipantUi,
    webRtcData: WebRtcData?,
    eglBase: EglBase?,
    floatingWidth: Dp,
    floatingHeight: Dp,
    onDoubleTap: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = floatingWidth, height = floatingHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onDoubleTap() }
                )
            }
    ) {
        ParticipantCallingView(
            modifier = Modifier.fillMaxSize(),
            eglBase = eglBase,
            webRtcData = webRtcData ?: WebRtcData(),
            participant = participant,
            scaleType = VideoScaleType.ASPECT_FILL
        )
    }
}