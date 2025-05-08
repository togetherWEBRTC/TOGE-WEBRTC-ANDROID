package example.beechang.together.ui.utils

import android.util.Log
import androidx.compose.runtime.MutableState
import org.webrtc.EglBase
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

class WebRtcLifecycleHandler(
    private val userId: String,
    private val videoTrack: MutableState<VideoTrack?>,
    private val surfaceViewRenderer: MutableState<SurfaceViewRenderer?>,
    private val isInitialized: MutableState<Boolean>,
    private val eglBase: EglBase?
) {

    fun onResume(newVideoTrack: VideoTrack?) {
        surfaceViewRenderer.value?.let { renderer ->
            if (isInitialized.value && eglBase?.eglBaseContext != null) {
                try {
                    if (newVideoTrack != null) {
                        videoTrack.value = newVideoTrack
                        newVideoTrack.addSink(renderer)
                    } else {
                        videoTrack.value?.addSink(renderer)
                        Log.d("WebRtcLifecycleHandler", "$userId's existing sink resumed")
                    }
                } catch (e: Exception) {
                    Log.e("WebRtcLifecycleHandler", "Error adding sink on resume for $userId", e)
                }
            } else {
                Log.e("WebRtcLifecycleHandler", "$userId - not initialized or no EGL context")
            }
        }
    }

    fun onPause() {
        surfaceViewRenderer.value?.let { renderer ->
            try {
                videoTrack.value?.removeSink(renderer)
            } catch (e: Exception) {
                Log.e("WebRtcLifecycleHandler", "Error removing sink on pause for $userId", e)
            }
        }
    }

    fun onDestroy() {
        surfaceViewRenderer.value?.let { renderer ->
            try {
                videoTrack.value?.removeSink(renderer)
                renderer.release()
                isInitialized.value = false
            } catch (e: Exception) {
                Log.e("WebRtcLifecycleHandler", "Error cleaning up renderer for $userId", e)
            }
        }
        surfaceViewRenderer.value = null
        videoTrack.value = null
    }
}