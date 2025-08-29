package example.beechang.together.ui.utils

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RemoteConfigManager(
    private val keys: List<String>
) {
    private val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
    private val _values: MutableMap<String, String> = mutableMapOf()

    fun init(onReady: () -> Unit = {}) {
        CoroutineScope(Dispatchers.IO).launch {
            remoteConfig.fetchAndActivate().await()
            keys.forEach { key ->
                _values[key] = remoteConfig.getString(key)
            }
            onReady()
        }
    }

    fun getValue(key: String): String? = _values[key]
}

enum class RemoteConfigKey(val keyName: String) {
    PRIVACY_POLICY("privacy_policy"),
    TERMS_OF_SERVICE("terms_of_service"),
}