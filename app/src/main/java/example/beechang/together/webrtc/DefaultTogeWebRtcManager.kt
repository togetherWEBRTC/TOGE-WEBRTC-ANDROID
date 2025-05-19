package example.beechang.together.webrtc

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import example.beechang.together.webrtc.peerconnection.TogePeerConnectionFactory
import example.beechang.together.webrtc.media.TogeVideoHandler
import example.beechang.together.webrtc.media.VideoResolutionManager
import example.beechang.together.webrtc.peerconnection.TogePeerConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.VideoSource
import org.webrtc.VideoTrack
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class DefaultTogeWebRtcManager @Inject constructor(
    @ApplicationContext private val context: Context,
) : TogeWebRtcManager {

    override val eglBase: EglBase = EglBase.create()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val pcf: TogePeerConnectionFactory = TogePeerConnectionFactory(context, eglBase)

    private val videoHandler by lazy { TogeVideoHandler(context = context, eglBase = eglBase) }

    private val videoResolutionManager: VideoResolutionManager = VideoResolutionManager()

    private val _participantMapFlow = MutableStateFlow<Map<String, WebRtcData>>(emptyMap())
    override val participantMapFlow: StateFlow<Map<String, WebRtcData>> = _participantMapFlow

    private val _signallingEventFlow = MutableSharedFlow<SignallingEvent>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val signallingEventFlow: SharedFlow<SignallingEvent> = _signallingEventFlow

    private val remoteUserPeerConnection = mutableMapOf<String/*userId*/, TogePeerConnection>()

    private val pendingIceCandidates =
        ConcurrentHashMap<String/*userId*/, MutableList<IceCandidate>>()

    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var isSpeakerMuted: Boolean = false

    override fun processAction(action: WebRtcAction) {
        action.process()
    }

    override fun processActionAsync(action: WebRtcAction) {
        coroutineScope.launch {
            action.process()
        }
    }

    private fun WebRtcAction.process() {
        when (this) {
            is WebRtcAction.General -> processGeneralAction(this)
            is WebRtcAction.Signaling -> processSignalingAction(this)
        }
    }

    private fun processGeneralAction(action: WebRtcAction.General) {
        when (action) {
            is WebRtcAction.General.InitWebRtc -> {
                initWebRtc(userId = action.userId)
            }

            is WebRtcAction.General.CreatePeerConnection -> {
                createPeerConnection(
                    remoteUserId = action.userId,
                    role = action.role
                )
            }

            is WebRtcAction.General.SwitchCamera -> {
                videoHandler.switchCamera()
                updateParticipant(action.userId) { participant ->
                    participant.copy(
                        isFrontLocalCamera = videoHandler.getIsUsingFrontCamera()
                    )
                }
            }

            is WebRtcAction.General.RemoveParticipant -> {
                removeParticipant(action.userId)
            }

            is WebRtcAction.General.ToggleAudio -> {
                localAudioTrack?.setEnabled(action.enabled)
            }

            is WebRtcAction.General.ToggleVideo -> {
                localVideoTrack?.setEnabled(action.enabled)
            }

            is WebRtcAction.General.RefreshAudio -> {
                refreshAudioTrack(action.userId)
            }

            is WebRtcAction.General.RefreshVideo -> {
                refreshVideoTrack(action.userId)
            }

            is WebRtcAction.General.SetSpeakerMute -> {
                setSpeakerMute(action.userId, action.isMuted)
            }
        }
    }

    private fun processSignalingAction(action: WebRtcAction.Signaling) {
        when (action) {
            is WebRtcAction.Signaling.SetIceCandidate -> {
                handleIceCandidate(
                    userId = action.userId,
                    sdp = action.sdp,
                    sdpMid = action.sdpMid,
                    sdpMLineIndex = action.sdpMLineIndex
                )
            }

            is WebRtcAction.Signaling.SetOfferDescription -> {
                remoteUserPeerConnection[action.userId]?.let {
                    //순서 중요 : PeerConnection cannot create an answer in a state other than have-remote-offer or have-local-pranswer.
                    setRemoteDescription(
                        remoteUserId = action.userId,
                        sdp = action.sdp,
                        isOffer = true
                    )
                    it.createAnswer()
                }
            }

            is WebRtcAction.Signaling.SetAnswerDescription -> {
                setRemoteDescription(
                    remoteUserId = action.userId,
                    sdp = action.sdp,
                    isOffer = false
                )
            }
        }
    }

    override fun release() {
        try {
            videoHandler.release()
            localVideoTrack?.dispose()
            localVideoTrack = null

            localAudioTrack?.dispose()
            localAudioTrack = null

            isSpeakerMuted = false

            remoteUserPeerConnection.values.forEach { it.release() }
            remoteUserPeerConnection.clear()

            videoResolutionManager.release()

            pendingIceCandidates.clear()

            _participantMapFlow.value = emptyMap()

            coroutineScope.cancel()
        } catch (e: Exception) {
            Log.e("TogeWebRtcManager", "Error releasing resources: ${e.message}")
        }
    }

    private fun createPeerConnection(remoteUserId: String, role: PeerConnectionRole) {
        val togePC = TogePeerConnection(
            pcf = pcf.pcf,
            updatedVideoTrack = { track ->
                updateParticipant(remoteUserId) { participant -> participant.copy(videoTrack = track) }
                setupVideoTrackResolutionObserver(track, remoteUserId)
            },
            updatedAudioTrack = { track ->
                track.setEnabled(!isSpeakerMuted)
                updateParticipant(remoteUserId) { participant -> participant.copy(audioTrack = track) }
            },
            emitIceCandidate = { sdp, sdpMid, sdpMLineIndex ->
                coroutineScope.launch {
                    _signallingEventFlow.emit(
                        SignallingEvent.SendIceCandidate(
                            toReceiver = remoteUserId,
                            sdp = sdp,
                            sdpMid = sdpMid,
                            sdpMLineIndex = sdpMLineIndex
                        )
                    )
                }
            },
            sendOffer = { sdp ->
                coroutineScope.launch {
                    _signallingEventFlow.emit(
                        SignallingEvent.SendOffer(toReceiver = remoteUserId, sdp = sdp)
                    )
                }
            },
            sendAnswer = { sdp ->
                coroutineScope.launch {
                    _signallingEventFlow.emit(
                        SignallingEvent.SendAnswer(toReceiver = remoteUserId, sdp = sdp)
                    )
                }
            }
        ).apply {
            createPeerConnection()
            remoteUserPeerConnection[remoteUserId] = this
            addLocalAudioTrack(localAudioTrack)
            addLocalVideoTrack(localVideoTrack)
            if (role == PeerConnectionRole.Offerer) {
                createOffer()
            }
        }
    }

    private fun initWebRtc(userId: String) {
        val videoSource = createVideoSource()
        localVideoTrack = createVideoTrack(userId, videoSource)

        val audioSource = pcf.createAudioSource()
        localAudioTrack = createAudioTrack(audioSource)

        updateParticipant(userId) { participant ->
            participant.copy(
                videoTrack = localVideoTrack,
                audioTrack = localAudioTrack,
                isFrontLocalCamera = videoHandler.getIsUsingFrontCamera()
            )
        }

        isSpeakerMuted = false
    }

    private fun createVideoSource(): VideoSource = pcf.createVideoSource().apply {
        videoHandler.initializeVideoCapturer(capturerObserver = this.capturerObserver)
        videoHandler.startVideoCapture()
    }

    private fun createVideoTrack(
        userId: String,
        videoSource: VideoSource,
    ): VideoTrack? =
        pcf.createVideoTrack(videoSource = videoSource).also { track ->
            track?.let {
                track.setEnabled(true)
                setupVideoTrackResolutionObserver(it, userId)
                remoteUserPeerConnection.forEach {
                    it.value.run {
                        addLocalVideoTrack(track)
                    }
                }
            }
        }

    private fun createAudioTrack(
        audioSource: AudioSource,
    ): AudioTrack? =
        pcf.createAudioTrack(audioSource = audioSource).also { track ->
            track?.let {
                track.setEnabled(true)
                remoteUserPeerConnection.forEach {
                    it.value.run {
                        addLocalAudioTrack(track)
                    }
                }
            }
        }

    private fun refreshVideoTrack(userId: String) {
        localVideoTrack?.let {
            videoResolutionManager.removeTrack(it)
        }

        localVideoTrack = createVideoTrack(
            userId = userId,
            videoSource = createVideoSource()
        ).also { newVideoTrack ->
            updateParticipant(userId) { participant ->
                participant.copy(videoTrack = newVideoTrack)
            }
            setupVideoTrackResolutionObserver(newVideoTrack, userId)
        }
    }

    private fun refreshAudioTrack(userId: String) {
        localAudioTrack = createAudioTrack(
            audioSource = pcf.createAudioSource()
        ).also { newAudioTrack ->
            updateParticipant(userId) { participant ->
                participant.copy(audioTrack = newAudioTrack)
            }
        }
    }

    private fun setSpeakerMute(userId: String, isMuted: Boolean) {
        isSpeakerMuted = isMuted
        updateParticipant(userId) { participant ->
            participant.copy(isSpeakerMuted = isMuted)
        }
        _participantMapFlow.value
            .filter { it.key != userId }
            .forEach { (_, participant) ->
                participant.audioTrack?.setEnabled(!isMuted)
            }
    }

    private fun setupVideoTrackResolutionObserver(videoTrack: VideoTrack?, userId: String) {
        videoTrack?.let { track ->
            videoResolutionManager.addTrackForResolutions(track) { width, height ->
                val isPortrait =
                    context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
                val (adjustedWidth, adjustedHeight) = if (isPortrait && width > height) {
                    Pair(height, width)
                } else {
                    Pair(width, height)
                }
                updateParticipant(userId) { participant ->
                    participant.copy(
                        resolutionWidth = adjustedWidth,
                        resolutionHeight = adjustedHeight
                    )
                }
            }
        }
    }

    private fun handleIceCandidate(
        userId: String,
        sdp: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ) {
        val peerConnection = remoteUserPeerConnection[userId] ?: return
        if (peerConnection.hasRemoteDescription()) {
            peerConnection.setIceCandidate(sdp, sdpMid, sdpMLineIndex)
        } else {
            pendingIceCandidates.getOrPut(userId) {
                Collections.synchronizedList(mutableListOf())
            }.add(IceCandidate(sdpMid, sdpMLineIndex, sdp))
        }
    }

    private fun updateParticipant(
        userId: String,
        createIfMissing: Boolean = true,
        update: (WebRtcData) -> WebRtcData
    ) {
        _participantMapFlow.update { currentMap ->
            val participant = currentMap[userId]
            if (participant != null) {
                currentMap + (userId to update(participant))
            } else {
                if (createIfMissing) {
                    currentMap + (userId to update(WebRtcData(userId = userId)))
                } else {
                    throw IllegalStateException("Participant $userId not found")
                }
            }
        }
    }

    private fun removeParticipant(userId: String) {
        _participantMapFlow.update { currentMap ->
            currentMap.toMutableMap().apply {
                remove(userId)
            }
        }
        remoteUserPeerConnection.remove(userId)
        pendingIceCandidates.remove(userId)
    }

    private fun setRemoteDescription(remoteUserId: String, sdp: String, isOffer: Boolean) {
        remoteUserPeerConnection[remoteUserId]?.setRemoteDescription(
            sdp = sdp,
            isOffer = isOffer
        )
        applyPendingIceCandidates(remoteUserId)
    }

    private fun applyPendingIceCandidates(userId: String) {
        val candidates = pendingIceCandidates[userId] ?: return
        val peerConnection = remoteUserPeerConnection[userId] ?: return

        val candidatesToApply = synchronized(candidates) {
            val copy = candidates.toList()
            candidates.clear()
            copy
        }
        candidatesToApply.forEach { candidate ->
            peerConnection.setIceCandidate(
                sdp = candidate.sdp,
                sdpMid = candidate.sdpMid,
                sdpMLineIndex = candidate.sdpMLineIndex
            )
        }
    }

}