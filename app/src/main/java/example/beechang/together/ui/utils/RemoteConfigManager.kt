package example.beechang.together.ui.utils

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.tasks.await

object RemoteConfigManager {
    private val remoteConfig get() = Firebase.remoteConfig
    private val _values: MutableMap<RemoteConfigKey, String> = mutableMapOf()

    suspend fun init(keys: List<RemoteConfigKey> = RemoteConfigKey.entries) {
        remoteConfig.fetchAndActivate().await()
        keys.forEach { key ->
            _values[key] = remoteConfig.getString(key.keyName)
        }
    }

    fun getValue(key: RemoteConfigKey): String? {
        return _values[key] ?: remoteConfig.getString(key.keyName)
    }

    suspend fun refreshKey(key: RemoteConfigKey) {
        remoteConfig.fetchAndActivate().await()
        _values[key] = remoteConfig.getString(key.keyName)
    }
}

enum class RemoteConfigKey(val keyName: String) {
    PRIVACY_POLICY("privacy_policy"),
    TERMS_OF_SERVICE("terms_of_service"),
}