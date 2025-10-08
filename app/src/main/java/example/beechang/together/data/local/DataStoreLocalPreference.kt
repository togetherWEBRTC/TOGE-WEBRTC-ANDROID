package example.beechang.together.data.local

import example.beechang.together.domain.data.LocalPreference
import javax.inject.Inject
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import example.beechang.together.data.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


@Singleton
class DataStoreLocalPreference @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationScope private val appScope: CoroutineScope,
) : LocalPreference {

    private var _accessToken: String = ""

    private var _refreshToken: String = ""

    private var _uid: String = ""

    private var _nickname: String = ""

    private var _profileUrl: String = ""

    private var _loginState: String = ""

    private var _accessTokenIat: Long = 0L

    private var _sessionId: String = ""

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

    init {
        dataStore.data
            .onEach { preferences ->
                _accessToken = preferences[ACCESS_TOKEN] ?: ""
                _refreshToken = preferences[REFRESH_TOKEN] ?: ""
                _uid = preferences[UID] ?: ""
                _nickname = preferences[NICKNAME] ?: ""
                _profileUrl = preferences[PROFILE_URL] ?: ""
                _loginState = preferences[LOGIN_STATE] ?: ""
                _accessTokenIat = preferences[ACCESS_TOKEN_IAT] ?: 0L
                _sessionId = preferences[SESSION_ID] ?: ""
            }
            .launchIn(appScope)
    }

    override var accessToken: String
        get() = _accessToken
        set(value) {
            _accessToken = value
            appScope.launch { dataStore.edit { it[ACCESS_TOKEN] = value } }
        }

    override var refreshToken: String
        get() = _refreshToken
        set(value) {
            _refreshToken = value
            appScope.launch { dataStore.edit { it[REFRESH_TOKEN] = value } }
        }

    override var uid: String
        get() = _uid
        set(value) {
            _uid = value
            appScope.launch { dataStore.edit { it[UID] = value } }
        }

    override var nickname: String
        get() = _nickname
        set(value) {
            _nickname = value
            appScope.launch { dataStore.edit { it[NICKNAME] = value } }
        }

    override var profileUrl: String
        get() = _profileUrl
        set(value) {
            _profileUrl = value
            appScope.launch { dataStore.edit { it[PROFILE_URL] = value } }
        }

    override var loginState: String
        get() = _loginState
        set(value) {
            _loginState = value
            appScope.launch { dataStore.edit { it[LOGIN_STATE] = value } }
        }

    override var accessTokenIat: Long
        get() = _accessTokenIat
        set(value) {
            _accessTokenIat = value
            appScope.launch { dataStore.edit { it[ACCESS_TOKEN_IAT] = value } }
        }

    override var sessionId: String
        get() = _sessionId
        set(value) {
            _sessionId = value
            appScope.launch { dataStore.edit { it[SESSION_ID] = value } }
        }

    override fun clear() {
        _accessToken = ""
        _refreshToken = ""
        _uid = ""
        _nickname = ""
        _profileUrl = ""
        _accessTokenIat = 0L

        appScope.launch {
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN)
                preferences.remove(REFRESH_TOKEN)
                preferences.remove(UID)
                preferences.remove(NICKNAME)
                preferences.remove(PROFILE_URL)
                preferences.remove(ACCESS_TOKEN_IAT)
            }
        }
    }

    override fun logout(type: String) {
        _loginState = type
        _accessToken = ""
        _refreshToken = ""
        _uid = ""
        _nickname = ""
        _profileUrl = ""
        _accessTokenIat = 0L

        appScope.launch {
            dataStore.edit { preferences ->
                preferences[LOGIN_STATE] = type
                preferences[ACCESS_TOKEN] = ""
                preferences[REFRESH_TOKEN] = ""
                preferences[UID] = ""
                preferences[NICKNAME] = ""
                preferences[PROFILE_URL] = ""
                preferences[ACCESS_TOKEN_IAT] = 0L
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
        private val SESSION_ID = stringPreferencesKey("sessionId")
    }
}