package example.beechang.together.webrtc.peerconnection

import android.content.Context
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnectionFactory
import org.webrtc.VideoSource
import org.webrtc.VideoTrack


class TogePeerConnectionFactory(
    private val context: Context,
    private val eglBase: EglBase
) {

    private val factory: PeerConnectionFactory by lazy {
        initializeFactory()
    }

    val pcf: PeerConnectionFactory
        get() = factory

    fun createVideoSource(isScreenCast: Boolean/* 화면공유용 */ = false): VideoSource =
        factory.createVideoSource(isScreenCast)

    fun createAudioSource(media: MediaConstraints = MediaConstraints()): AudioSource =
        factory.createAudioSource(media)

    fun createVideoTrack(
        videoSource: VideoSource,
        trackId: String = "VideoTrack${System.currentTimeMillis()}"
    ): VideoTrack? = factory.createVideoTrack(trackId, videoSource)

    fun createAudioTrack(
        audioSource: AudioSource,
        trackId: String = "AudioTrack${System.currentTimeMillis()}"
    ): AudioTrack? = factory.createAudioTrack(trackId, audioSource)

    private fun initializeFactory(): PeerConnectionFactory {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .createInitializationOptions()
        )

        val options = PeerConnectionFactory.Options()
        return PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true)
            )
            .setVideoDecoderFactory(
                DefaultVideoDecoderFactory(eglBase.eglBaseContext)
            )
            .createPeerConnectionFactory()
    }

}