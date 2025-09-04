package example.beechang.together.ui.utils

import android.util.Log
import androidx.compose.runtime.MutableState
import example.beechang.together.ui.component.util.webrtc.TogeTextureViewRenderer

import org.webrtc.VideoTrack

class WebRtcLifecycleHandler(
    private val userId: String,
    private val videoTrack: MutableState<VideoTrack?>,
    private val renderer: MutableState<TogeTextureViewRenderer?>,
    private val isInitialized: MutableState<Boolean>
) {
    fun onResume(newVideoTrack: VideoTrack?) {
        renderer.value?.let { rend ->
            if (isInitialized.value) {
                try {
                    if (newVideoTrack != null && videoTrack.value != newVideoTrack) {
                        videoTrack.value?.removeSink(rend)
                        videoTrack.value = newVideoTrack
                        newVideoTrack.addSink(rend)
                    } else {
                        videoTrack.value?.addSink(rend)
                    }
                } catch (e: Exception) {
                    Log.e("WebRtcLifecycleHandler", "Error adding sink on resume for $userId", e)
                }
            } else {
                Log.e("WebRtcLifecycleHandler", "$userId - renderer not initialized on resume.")
            }
        }
    }

    fun onPause() {
        renderer.value?.let { rend ->
            try {
                videoTrack.value?.removeSink(rend)
            } catch (e: Exception) {
                Log.e("WebRtcLifecycleHandler", "Error removing sink on pause for $userId", e)
            }
        }
    }

    fun onDestroy() {
        renderer.value?.let { rend ->
            try {
                videoTrack.value?.removeSink(rend)
                rend.release()
                isInitialized.value = false
            } catch (e: Exception) {
                Log.e("WebRtcLifecycleHandler", "Error cleaning up renderer for $userId", e)
            }
        }
        renderer.value = null
        videoTrack.value = null
    }
}