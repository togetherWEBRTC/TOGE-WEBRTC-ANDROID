package example.beechang.together.data.local

import example.beechang.together.domain.data.LocalPreference
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


@Singleton
class DataStoreLocalPreference @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : LocalPreference {

    override val accessTokenFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[ACCESS_TOKEN] ?: ""
        }

    override val refreshTokenFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[REFRESH_TOKEN] ?: ""
        }

    override val uidFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[UID] ?: ""
        }

    override val nicknameFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[NICKNAME] ?: ""
        }

    override val profileUrlFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[PROFILE_URL] ?: ""
        }

    override val loginStateFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[LOGIN_STATE] ?: ""
        }

    override var accessToken: String
        get() = runBlocking { accessTokenFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN] = value
            }
        }

    override var refreshToken: String
        get() = runBlocking { refreshTokenFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[REFRESH_TOKEN] = value
            }
        }

    override var uid: String
        get() = runBlocking { uidFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[UID] = value
            }
        }

    override var nickname: String
        get() = runBlocking { nicknameFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[NICKNAME] = value
            }
        }

    override var profileUrl: String
        get() = runBlocking { profileUrlFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[PROFILE_URL] = value
            }
        }

    override var loginState: String
        get() = runBlocking { loginStateFlow.first() }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[LOGIN_STATE] = value
            }
        }

    override var accessTokenIat: Long
        get() = runBlocking { dataStore.data.first()[ACCESS_TOKEN_IAT] ?: 0L }
        set(value) = runBlocking {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_IAT] = value
            }
        }

    override fun clear() {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN] = ""
                preferences[REFRESH_TOKEN] = ""
                preferences[UID] = ""
                preferences[NICKNAME] = ""
                preferences[PROFILE_URL] = ""
            }
        }
    }

    override fun logout(type: String) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[LOGIN_STATE] = type
                preferences[ACCESS_TOKEN] = ""
                preferences[REFRESH_TOKEN] = ""
                preferences[UID] = ""
                preferences[NICKNAME] = ""
                preferences[PROFILE_URL] = ""
            }
        }
    }

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("accessToken")
        private val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
        private val UID = stringPreferencesKey("userId")
        private val NICKNAME = stringPreferencesKey("nickname")
        private val PROFILE_URL = stringPreferencesKey("profileUrl")
        private val LOGIN_STATE = stringPreferencesKey("loginState")
        private val ACCESS_TOKEN_IAT = longPreferencesKey("accessTokenIat")
    }
}