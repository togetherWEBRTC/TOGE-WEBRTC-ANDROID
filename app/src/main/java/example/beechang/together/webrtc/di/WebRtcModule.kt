package example.beechang.together.webrtc.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import example.beechang.together.webrtc.TogeWebRtcManagerImpl
import example.beechang.together.webrtc.TogeWebRtcManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebRtcModule {

    @Singleton
    @Binds
    abstract fun bindWebRtcManager(
        implementation: TogeWebRtcManagerImpl
    ): TogeWebRtcManager
}