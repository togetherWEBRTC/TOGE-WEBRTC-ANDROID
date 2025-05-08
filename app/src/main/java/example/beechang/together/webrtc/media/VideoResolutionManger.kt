package example.beechang.together.webrtc.media

import org.webrtc.VideoFrame
import org.webrtc.VideoSink
import org.webrtc.VideoTrack

class VideoResolutionManager {

    private val trackSinks = mutableMapOf<VideoTrack, VideoSink>()

    fun addTrackForResolutions(track: VideoTrack , callback: (width: Int, height: Int) -> Unit) {
        removeTrack(track)

        val sink = object : VideoSink {
            private var lastWidth = 0
            private var lastHeight = 0

            override fun onFrame(frame: VideoFrame) {
                val width = frame.buffer.width
                val height = frame.buffer.height

                if (width != lastWidth || height != lastHeight) {
                    lastWidth = width
                    lastHeight = height
                    callback(width, height)
                }
            }
        }

        track.addSink(sink)
        trackSinks[track] = sink
    }

    fun removeTrack(track: VideoTrack) {
        trackSinks[track]?.let { sink ->
            track.removeSink(sink)
            trackSinks.remove(track)
        }
    }

    fun release() {
        trackSinks.forEach { (track, sink) ->
            track.removeSink(sink)
        }
        trackSinks.clear()
    }
}

