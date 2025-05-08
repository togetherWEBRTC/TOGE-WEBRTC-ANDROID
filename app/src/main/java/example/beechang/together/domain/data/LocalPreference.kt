package example.beechang.together.domain.data

import kotlinx.coroutines.flow.Flow

interface LocalPreference {
    var accessToken: String
    var refreshToken: String
    var uid: String
    var nickname: String
    var profileUrl: String
    var loginState: String
    var accessTokenIat: Long

    val accessTokenFlow: Flow<String>
    val refreshTokenFlow: Flow<String>
    val uidFlow: Flow<String>
    val nicknameFlow: Flow<String>
    val profileUrlFlow: Flow<String>
    val loginStateFlow: Flow<String>
    fun clear()
    fun logout(type: String)
}