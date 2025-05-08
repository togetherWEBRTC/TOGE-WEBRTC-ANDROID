package example.beechang.together.ui.component.util.webrtc

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.VideoScaleType
import example.beechang.together.webrtc.WebRtcData
import org.webrtc.EglBase

@Composable
fun TwoParticipantsFloatingLayout(
    localUserId: String,
    participants: LinkedHashMap<String, RoomParticipantUi>,
    eglBase: EglBase?,
    webRtcData: Map<String, WebRtcData>,
    onSwapParticipants: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val remoteEntry = participants.entries.firstOrNull { it.key != localUserId } ?: return
    val remoteUserId = remoteEntry.key

    var mainViewUserId by remember { mutableStateOf(remoteUserId) }
    var floatingViewUserId by remember { mutableStateOf(localUserId) }

    val boxSize = remember { mutableStateOf(IntSize(0, 0)) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { boxSize.value = it }
    ) {
        val density = LocalDensity.current
        val containerWidthDp = with(density) { boxSize.value.width.toDp() }
        val floatingWidth = containerWidthDp * 0.3f
        val floatingHeight = floatingWidth * 4f / 3f

        val mainParticipant = participants[mainViewUserId] ?: return
        val floatingParticipant = participants[floatingViewUserId] ?: return

         ParticipantCallingView(
            participant = mainParticipant,
            webRtcData = webRtcData[mainViewUserId] ?: WebRtcData(),
            eglBase = eglBase,
            scaleType = VideoScaleType.ASPECT_FIT,
            modifier = Modifier.fillMaxSize()
        )

        if (boxSize.value.width > 0) {
            // 우측 하단에 고정된 플로팅 뷰
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                FloatingVideoView(
                    participant = floatingParticipant,
                    webRtcData = webRtcData[floatingViewUserId],
                    eglBase = eglBase,
                    floatingWidth = floatingWidth,
                    floatingHeight = floatingHeight,
                    onDoubleTap = {
                        val newMain = floatingViewUserId
                        val newFloat = mainViewUserId
                        mainViewUserId = newMain
                        floatingViewUserId = newFloat
                        onSwapParticipants(newMain, newFloat)
                    }
                )
            }
        }
    }
}