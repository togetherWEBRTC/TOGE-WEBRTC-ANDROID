package example.beechang.together.webrtc.media

import example.beechang.together.webrtc.media.VADMonitor.Companion.AUDIO_LEVEL_THRESHOLD
import example.beechang.together.webrtc.media.VADMonitor.Companion.SILENCE_DELAY_MS
import example.beechang.together.webrtc.media.VADMonitor.Companion.SPEAKING_DELAY_MS
import example.beechang.together.webrtc.media.VADMonitor.Companion.STATS_PULL_INTERVAL_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.webrtc.PeerConnection
import org.webrtc.RTCStatsReport
import java.util.concurrent.ConcurrentHashMap


interface VADMonitor {
    val speakingStatusFlow: StateFlow<Map<String, Boolean>>

    fun start(localUserId: String)
    fun stop()
    fun addPeer(userId: String, peerConnection: PeerConnection)
    fun removePeer(userId: String)

    companion object {
        const val AUDIO_LEVEL_THRESHOLD = 0.08
        const val SPEAKING_DELAY_MS = 300L
        const val SILENCE_DELAY_MS = 800L
        const val STATS_PULL_INTERVAL_MS = 300L
    }
}

class VADMonitorImpl : VADMonitor {

    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var monitoringJob: Job? = null

    private var localUserId: String? = null

    private val _speakingStatusFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    override val speakingStatusFlow: StateFlow<Map<String, Boolean>> =
        _speakingStatusFlow.asStateFlow()

    private val peerConnectionMap = ConcurrentHashMap<String, PeerConnection>()
    private val audioLevelMap = ConcurrentHashMap<String, AudioLevelState>()

    private data class AudioLevelState(
        var isSpeaking: Boolean = false,
        var lastSpokeAt: Long = 0L,
        var lastSilentAt: Long = 0L
    )

    override fun start(localUserId: String) {
        if (monitoringJob?.isActive == true) return
        this.localUserId = localUserId

        monitoringJob = monitorScope.launch {
            while (isActive) {
                peerConnectionMap.forEach { (remoteUserId, peerConnection) ->
                    peerConnection.getStats { report ->
                        processRemoteAudioStats(remoteUserId, report)
                    }
                }
                peerConnectionMap.values.firstOrNull()?.getStats { report ->
                    processLocalAudioStats(report)
                }
                delay(STATS_PULL_INTERVAL_MS)
            }
        }
    }

    override fun stop() {
        monitoringJob?.cancel()
        monitoringJob = null
        localUserId = null
        peerConnectionMap.clear()
        audioLevelMap.clear()
        _speakingStatusFlow.value = emptyMap()
    }

    override fun addPeer(userId: String, peerConnection: PeerConnection) {
        peerConnectionMap[userId] = peerConnection
    }

    override fun removePeer(userId: String) {
        peerConnectionMap.remove(userId)
        audioLevelMap.remove(userId)
        _speakingStatusFlow.update { it - userId }
    }

    private fun processRemoteAudioStats(remoteUserId: String, report: RTCStatsReport?) {
        report?.statsMap?.values?.firstOrNull {
            it.type == "inbound-rtp" && it.members["kind"] == "audio"
        }?.let { stats ->
            val audioLevel = (stats.members["audioLevel"] as? Double) ?: 0.0
            updateSpeakingStatus(remoteUserId, audioLevel)
        }
    }

    private fun processLocalAudioStats(report: RTCStatsReport?) {
        val currentLocalUserId = localUserId ?: return
        report?.statsMap?.values?.firstOrNull {
            it.type == "media-source" && it.members["kind"] == "audio"
        }?.let { stats ->
            val audioLevel = (stats.members["audioLevel"] as? Double) ?: 0.0
            updateSpeakingStatus(currentLocalUserId, audioLevel)
        }
    }

    private fun updateSpeakingStatus(userId: String, level: Double) {
        val now = System.currentTimeMillis()
        val state = audioLevelMap.getOrPut(userId) { AudioLevelState() }

        if (level > AUDIO_LEVEL_THRESHOLD) {
            state.lastSpokeAt = now
            if (!state.isSpeaking && (now - state.lastSilentAt) > SPEAKING_DELAY_MS) {
                state.isSpeaking = true
                updateFlow(userId, true)
            }
        } else {
            state.lastSilentAt = now
            if (state.isSpeaking && (now - state.lastSpokeAt) > SILENCE_DELAY_MS) {
                state.isSpeaking = false
                updateFlow(userId, false)
            }
        }
    }

    private fun updateFlow(userId: String, isSpeaking: Boolean) {
        _speakingStatusFlow.update { currentStatus ->
            if (currentStatus[userId] == isSpeaking) currentStatus else {
                currentStatus + (userId to isSpeaking)
            }
        }
    }

}