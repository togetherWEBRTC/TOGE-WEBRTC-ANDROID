package example.beechang.together.webrtc.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import example.beechang.together.webrtc.DefaultTogeWebRtcManager
import example.beechang.together.webrtc.TogeWebRtcManager
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebRtcModule {

    @Singleton
    @Binds
    abstract fun bindWebRtcManagerFactory(
        implementation: DefaultTogeWebRtcManagerFactory
    ): TogeWebRtcManagerFactory
}

@Singleton
class DefaultTogeWebRtcManagerFactory @Inject constructor(
    @ApplicationContext private val context: Context
) : TogeWebRtcManagerFactory {

    override fun create(): TogeWebRtcManager {
        return DefaultTogeWebRtcManager(context)
    }
}

interface TogeWebRtcManagerFactory {
    fun create(): TogeWebRtcManager
}