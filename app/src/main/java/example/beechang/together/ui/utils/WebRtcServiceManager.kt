package example.beechang.together.ui.utils

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import example.beechang.together.webrtc.intent.TogeWebRtcIntent
import example.beechang.together.webrtc.service.WebRtcService

interface ServiceManager<T : Service> {
    val service: T?
    fun bindService(onServiceConnected: () -> Unit = {})
    fun stopService()
    fun unbindService()
}

val LocalWebRtcServiceManager = compositionLocalOf<WebRtcServiceManager> {
    error("WebRtcServiceManager not provided")
}

@Composable
fun rememberWebRtcServiceManager(
    activityClass: Class<out Activity>,
    context: Context
): WebRtcServiceManager {
    val webRtcServiceManager = remember(activityClass) {
        WebRtcServiceManager(
            activityClass = activityClass,
            context = context
        )
    }

    DisposableEffect(webRtcServiceManager) {
        webRtcServiceManager.bindService()
        onDispose {
            webRtcServiceManager.release()
        }
    }

    return webRtcServiceManager
}

class WebRtcServiceManager(
    private val activityClass: Class<out Activity>,
    private val context: Context
) : ServiceManager<WebRtcService> {
    private var _service: WebRtcService? = null
    override val service: WebRtcService? get() = _service

    private lateinit var serviceConnection: ServiceConnection

    private val webRtcIntents by lazy {
        createWebRtcIntents(context, activityClass)
    }

    fun startCall() {
        val (returnIntent, stopIntent) = webRtcIntents
        _service?.startCall(
            returnAppIntent = returnIntent,
            stopActionIntent = stopIntent
        )
    }

    fun restartCall() {
        _service?.let {
            it.stopCall()
            startCall()
        }
    }

    override fun bindService(onServiceConnected: () -> Unit) {
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                _service = (binder as WebRtcService.LocalBinder).getService()
                startCall()
                onServiceConnected()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                _service = null
            }
        }

        Intent(context, WebRtcService::class.java).also { intent ->
            context.startService(intent)
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun stopService() {
        _service?.stopCall()
    }

    override fun unbindService() {
        if (::serviceConnection.isInitialized) {
            try {
                context.unbindService(serviceConnection)
            } catch (e: IllegalArgumentException) {
            }
        }
    }

    fun release() {
        stopService()
        unbindService()
        _service = null
    }

    private fun createWebRtcIntents(
        context: Context,
        activityClass: Class<out Activity>
    ): Pair<Intent, Intent> {
        val returnAppIntent = Intent(context, activityClass).apply {
            action = TogeWebRtcIntent.START.value
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }

        val stopActionIntent = Intent(context, activityClass).apply {
            action = TogeWebRtcIntent.STOP.value
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        return returnAppIntent to stopActionIntent
    }
}