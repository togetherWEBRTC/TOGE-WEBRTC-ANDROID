package example.beechang.together

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import example.beechang.together.ui.MainNavigation
import example.beechang.together.ui.TogetherApp
import example.beechang.together.ui.utils.RemoteConfigManager
import example.beechang.together.webrtc.intent.TogeWebRtcIntent
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appEventViewModel: TogeAppEventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().setOnExitAnimationListener { provider ->
            lifecycleScope.launch {
                try {
                    RemoteConfigManager.init()
                } catch (e: Exception) {
                    Log.e("MainActivity", "RemoteConfig Failed : ", e)
                } finally {
                    provider.remove()
                }
            }
        }

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            TogetherApp {
                MainNavigation(activityClass = MainActivity::class.java)
            }
        }

    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        intent.action?.let {
            when (it) {
                TogeWebRtcIntent.START.value -> {}
                TogeWebRtcIntent.STOP.value -> {
                    appEventViewModel.onStopCallReceived()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}