package example.beechang.together.webrtc

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.VideoTrack

interface TogeWebRtcManager {
    val eglBase: EglBase
    val participantMapFlow: StateFlow<Map<String, WebRtcData>>
    val signallingEventFlow: SharedFlow<SignallingEvent>
    fun processAction(action: WebRtcAction)
    fun release()
}

data class WebRtcData(
    val userId: String = "",
    val videoTrack: VideoTrack? = null,
    val audioTrack: AudioTrack? = null,
    val resolutionWidth: Int = 0,
    val resolutionHeight: Int = 0,
    val isFrontLocalCamera: Boolean = false,
    val isSpeakerMuted: Boolean = false,
)

enum class PeerConnectionRole {
    Offerer,
    Answerer
}

sealed interface SignallingEvent {
    data class SendOffer(val toReceiver: String, val sdp: String) : SignallingEvent
    data class SendAnswer(val toReceiver: String, val sdp: String) : SignallingEvent
    data class SendIceCandidate(
        val toReceiver: String,
        val sdp: String,
        val sdpMid: String,
        val sdpMLineIndex: Int
    ) : SignallingEvent
}

sealed interface WebRtcAction {
    sealed interface Signaling : WebRtcAction {
        data class SetOfferDescription(val userId: String, val sdp: String) : Signaling
        data class SetAnswerDescription(val userId: String, val sdp: String) : Signaling
        data class SetIceCandidate(
            val userId: String,
            val sdp: String,
            val sdpMid: String,
            val sdpMLineIndex: Int
        ) : Signaling
    }

    sealed interface General : WebRtcAction {
        data class InitWebRtc(val userId: String) : General
        data class CreatePeerConnection(val userId: String, val role: PeerConnectionRole) : General
        data class SwitchCamera(val userId: String) : General
        data class RemoveParticipant(val userId: String) : General
        data class ToggleVideo(val userId: String, val enabled: Boolean) : General
        data class ToggleAudio(val userId: String, val enabled: Boolean) : General
        data class RefreshVideo(val userId: String) : General
        data class RefreshAudio(val userId: String) : General
        data class SetSpeakerMute(val userId: String, val isMuted: Boolean) : General
    }
}