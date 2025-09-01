package example.beechang.together

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import example.beechang.together.ui.utils.RemoteConfigManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            RemoteConfigManager.init()
        }

    }

}