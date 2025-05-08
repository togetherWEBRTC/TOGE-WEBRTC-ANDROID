package example.beechang.together.webrtc.peerconnection

import android.util.Log
import example.beechang.together.BuildConfig
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnection.SignalingState
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.VideoTrack

class TogePeerConnection(
    private val pcf: PeerConnectionFactory,
    private val updatedVideoTrack: (VideoTrack) -> Unit,
    private val updatedAudioTrack: (AudioTrack) -> Unit,
    private val emitIceCandidate: (String/*sdp*/, String/*sdpMid*/, Int/*sdpMLineIndex*/) -> Unit,
    private val sendOffer: (String/*discription*/) -> Unit,
    private val sendAnswer: (String/*discription*/) -> Unit,
) {

    private lateinit var peerConnection: PeerConnection
    private lateinit var offer: SessionDescription
    private lateinit var answer: SessionDescription

    private var isRemoteDiscriptionSet: Boolean = false

    private val tunserver: String  = BuildConfig.TURN_SERVER_URL
    private val turnServerUsername: String = BuildConfig.TURN_SERVER_USERNAME
    private val turnServerPassword: String = BuildConfig.TURN_SERVER_PASSWORD


    private val iceTurnServer: List<PeerConnection.IceServer> by lazy {
        buildList {
            add(PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer())
            if(tunserver.isNotEmpty() && turnServerUsername.isNotEmpty() && turnServerPassword.isNotEmpty()) {
                add(
                    PeerConnection.IceServer.builder(tunserver)
                        .setUsername(turnServerUsername)
                        .setPassword(turnServerPassword)
                        .createIceServer()
                )
            }
        }
    }

    private val rtcContig by lazy {
        PeerConnection.RTCConfiguration(iceTurnServer).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }
    }

    val constraints = MediaConstraints()

    fun hasRemoteDescription(): Boolean = isRemoteDiscriptionSet

    fun isFinishedOfferAnswerExchange(): Boolean {
        return if (this::peerConnection.isInitialized) {
            peerConnection.signalingState() == SignalingState.STABLE && isRemoteDiscriptionSet
        } else {
            false
        }
    }

    fun createPeerConnection() {
        peerConnection = pcf.createPeerConnection(rtcContig, pcObserver())
            ?: throw IllegalStateException("fail peerConnection")
    }

    fun addLocalVideoTrack(localVideoTrack: VideoTrack? = null) {
        localVideoTrack?.let {
            peerConnection.addTrack(it)
        }
    }

    fun addLocalAudioTrack(localAudioTrack: AudioTrack? = null) {
        localAudioTrack?.let { peerConnection.addTrack(it) }
    }

    fun createAnswer() {
        peerConnection.createAnswer(
            object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                    sessionDescription?.let {
                        val description = it.description
                        answer = it
                        setLocalDescription(sdp = description, isOffer = false)
                        sendAnswer.invoke(description)
                    }
                }

                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            }, constraints
        )
    }

    fun createOffer() {
        peerConnection.createOffer(
            object : SdpObserver {
                override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                    sessionDescription?.let {
                        val description = it.description
                        offer = it
                        setLocalDescription(sdp = description, isOffer = true)
                        sendOffer.invoke(description)
                    }
                }

                override fun onSetSuccess() {}
                override fun onCreateFailure(p0: String?) {}
                override fun onSetFailure(p0: String?) {}
            }, constraints
        )
    }

    fun setLocalDescription(sdp: String, isOffer: Boolean) {
        val sessionDescription = SessionDescription(
            if (isOffer) SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER,
            sdp
        )
        peerConnection.setLocalDescription(sdpObserver(), sessionDescription)
    }

    fun setRemoteDescription(sdp: String, isOffer: Boolean) {
        val sessionDescription = SessionDescription(
            if (isOffer) SessionDescription.Type.OFFER else SessionDescription.Type.ANSWER,
            sdp
        )
        peerConnection.setRemoteDescription(sdpObserver(), sessionDescription)
        isRemoteDiscriptionSet = true
    }

    fun setIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) =
        peerConnection.addIceCandidate(IceCandidate(sdpMid, sdpMLineIndex, sdp))

    fun getPeerConnection(): PeerConnection = peerConnection

    fun release() {
        try {
            peerConnection.apply {
                this.dispose()
            }
        } catch (e: Exception) {
            Log.e("TogePeerConnection", "Error during release: ${e.message}")
        }
    }

    private fun pcObserver() = object : PeerConnection.Observer {
        override fun onTrack(transceiver: RtpTransceiver?) {
            transceiver?.receiver?.track()?.let { track ->
                when (track) {
                    is VideoTrack -> {
                        updatedVideoTrack(track)
                    }

                    is AudioTrack -> {
                        updatedAudioTrack(track)
                    }
                }
            }
        }

        override fun onIceCandidate(ice: IceCandidate?) {
            ice?.let {
                emitIceCandidate(it.sdp, it.sdpMid, it.sdpMLineIndex)
            }
        }

        override fun onRenegotiationNeeded() {
            if (!isFinishedOfferAnswerExchange()) {
                return
            }

            createOffer()
        }

        override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
        override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
        override fun onIceConnectionReceivingChange(p0: Boolean) {}
        override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
        override fun onIceCandidatesRemoved(p0: Array<out IceCandidate?>?) {}
        override fun onAddStream(p0: MediaStream?) {}
        override fun onRemoveStream(p0: MediaStream?) {}
        override fun onDataChannel(p0: DataChannel?) {}
    }

    private fun sdpObserver(
        onCreateSuccess: (SessionDescription?) -> Unit = {},
        onSetSuccess: () -> Unit = {},
        onCreateFailure: (String?) -> Unit = {},
        onSetFailure: (String?) -> Unit = {}
    ) = object : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {
            onCreateSuccess(p0)
        }

        override fun onSetSuccess() {
            onSetSuccess()
        }

        override fun onCreateFailure(p0: String?) {
            (onCreateFailure(p0))
        }

        override fun onSetFailure(p0: String?) {
            onSetFailure(p0)
        }
    }
}