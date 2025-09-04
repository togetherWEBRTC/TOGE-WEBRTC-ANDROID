package example.beechang.together

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import example.beechang.together.ui.MainNavigation
import example.beechang.together.ui.TogetherApp
import example.beechang.together.webrtc.intent.TogeWebRtcIntent

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appEventViewModel: TogeAppEventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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