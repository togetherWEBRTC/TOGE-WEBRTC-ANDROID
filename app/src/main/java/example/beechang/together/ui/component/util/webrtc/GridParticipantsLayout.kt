package example.beechang.together.ui.component.util.webrtc

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.call.room.VideoScaleType
import example.beechang.together.webrtc.WebRtcData
import org.webrtc.EglBase

@Composable
fun GridParticipantsLayout(
    participants: List<RoomParticipantUi>,
    eglBase: EglBase?,
    webRtcData: Map<String, WebRtcData>,
    onParticipantSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val columns = when {
        participants.size <= 2 -> 1
        participants.size <= 6 -> 2
        else -> 3
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.fillMaxSize()
        ) {
            items(participants.size) { idx ->
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(4f / 3f)
                        .clickable {  }
                ) {
                    val item  = participants[idx]
                    ParticipantCallingView(
                        participant = item,
                        webRtcData = webRtcData[item.userId] ?: WebRtcData(),
                        eglBase = eglBase,
                        scaleType = VideoScaleType.ASPECT_FILL,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}