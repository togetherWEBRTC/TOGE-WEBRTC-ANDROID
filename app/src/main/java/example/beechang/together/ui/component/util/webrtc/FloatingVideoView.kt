package example.beechang.together.ui.component.util.webrtc

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.VideoScaleType
import example.beechang.together.ui.theme.LocalTogeAppColor
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
    val animatedBorderWidth by animateDpAsState(
        targetValue = if (participant.isSpeaking) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 300)
    )

    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(width = floatingWidth, height = floatingHeight)
            .clip(shape)
            .border(1.dp, Color.White.copy(alpha = 0.5f), shape)
            .border(
                width = animatedBorderWidth,
                color = LocalTogeAppColor.current.primary700,
                shape = shape
            )
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { onDoubleTap() }) }
    ) {
        ParticipantCallingView(
            modifier = Modifier,
            eglBase = eglBase,
            webRtcData = webRtcData ?: WebRtcData(),
            participant = participant,
            scaleType = VideoScaleType.ASPECT_FILL
        )
    }
}