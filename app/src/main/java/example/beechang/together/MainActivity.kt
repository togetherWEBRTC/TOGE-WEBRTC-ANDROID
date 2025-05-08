package example.beechang.together

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import example.beechang.together.ui.MainNavigation
import example.beechang.together.ui.TogetherApp


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TogetherApp {
                MainNavigation()
            }
        }
    }
}