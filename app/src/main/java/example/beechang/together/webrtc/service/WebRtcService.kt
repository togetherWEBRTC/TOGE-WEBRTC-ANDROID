package example.beechang.together.webrtc.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import example.beechang.together.R
import example.beechang.together.domain.usecase.room.DisconnectRoomUseCase
import example.beechang.together.webrtc.TogeWebRtcManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

@AndroidEntryPoint
class WebRtcService : Service() {
    private val binder = LocalBinder()

    @Inject
    lateinit var webRtcManager: TogeWebRtcManager

    @Inject
    lateinit var disconnectRoomUseCase: DisconnectRoomUseCase

    inner class LocalBinder : Binder() {
        fun getService(): WebRtcService = this@WebRtcService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        runBlocking {
            withTimeoutOrNull(3000) {
                supervisorScope {
                    launch(Dispatchers.IO) {
                        disconnectRoomUseCase.invoke()
                    }
                    launch(Dispatchers.IO) {
                        webRtcManager.release()
                    }
                }
            }
        }

        super.onTaskRemoved(rootIntent)
    }

    fun startCall(
        returnAppIntent: Intent,
        stopActionIntent: Intent,
    ) {
        val notification = createCallNotification(
            contentIntent = returnAppIntent,
            stopActionIntent = stopActionIntent
        )

        val foregroundServiceType = calculateForegroundServiceType()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && foregroundServiceType == 0) {
            stopSelf()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, foregroundServiceType)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    fun stopCall() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun calculateForegroundServiceType(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return 0
        }

        var type = 0

        if (hasPermission(Manifest.permission.CAMERA)) {
            type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
        }

        if (hasPermission(Manifest.permission.RECORD_AUDIO)) {
            type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        }

        return type
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun createCallNotification(
        contentIntent: Intent,
        stopActionIntent: Intent,
    ): Notification {
        val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        val mainPendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_NOTIFICATION_CONTENT_REQUEST_CODE,
            contentIntent,
            flag
        )

        val stopPendingIntent = PendingIntent.getActivity(
            this,
            PENDING_INTENT_NOTIFICATION_STOP_REQUEST_CODE,
            stopActionIntent,
            flag
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.together_call))
            .setContentText(getString(R.string.tap_to_return_to_call_screen))
            .setSmallIcon(R.mipmap.logo)
            .setContentIntent(mainPendingIntent)
            .setOngoing(true)
            .setSound(null)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                getString(R.string.end_call),
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        NotificationChannel(
            CHANNEL_ID,
            getString(R.string.video_call),
            NotificationManager.IMPORTANCE_LOW
        ).run {
            manager.createNotificationChannel(this)
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 11
        private const val PENDING_INTENT_NOTIFICATION_CONTENT_REQUEST_CODE = 10
        private const val PENDING_INTENT_NOTIFICATION_STOP_REQUEST_CODE = 12
        private const val CHANNEL_ID = "example.beechang.together.CHANNEL_FOREGROUND_SERVICE_WEBRTC"
    }
}