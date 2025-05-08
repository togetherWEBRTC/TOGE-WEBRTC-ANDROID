package example.beechang.together.data.repository

import android.util.Base64
import android.util.Log
import example.beechang.together.BuildConfig
import example.beechang.together.data.http.api.UserDataSource
import example.beechang.together.data.request.LoginRequest
import example.beechang.together.data.request.SignupRequest
import example.beechang.together.domain.data.LocalPreference
import example.beechang.together.domain.data.TogeError.InvalidAccessToken
import example.beechang.together.domain.data.TogeError.InvalidUserInfo
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.data.map
import example.beechang.together.domain.model.LoginState
import example.beechang.together.domain.model.UserInfo
import example.beechang.together.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userDataSource: UserDataSource,
    private val localPreference: LocalPreference
) : UserRepository {

    override suspend fun requestLogin(userId: String, password: String): TogeResult<Boolean> {
        return userDataSource.requestLogin(LoginRequest(userId, password)).onSuccess {
            localPreference.run {
                parseJwtPayload(it.accessToken ?: "")?.let { payload ->
                    uid = payload.getString("userId") ?: ""
                    nickname = payload.getString("nickname") ?: ""
                    profileUrl = payload.getString("profileUrl") ?: ""
                    accessTokenIat = payload.getLong("iat")
                }
                accessToken = it.accessToken ?: ""
                refreshToken = it.refreshToken ?: ""
                loginState = LoginState.Login.name
                Log.e("DefaultUserRepository", "accessToken: $accessToken")
            }
        }.map {
            it.toSuccessBoolean()
        }
    }

    override suspend fun checkUsableId(userId: String): TogeResult<Boolean> =
        userDataSource.checkUsableId(userId)

    override suspend fun requestSignUp(
        userId: String,
        nickname: String,
        password: String,
        passwordConfirm: String
    ): TogeResult<Boolean> =
        userDataSource.requestSignUp(
            signUpRequest = SignupRequest(userId, nickname, password, passwordConfirm)
        )

    override fun getLocalAccessTokenFlow(): Flow<TogeResult<String>> =
        localPreference.accessTokenFlow.map {
            if (it.isEmpty()) {
                TogeResult.Error(InvalidAccessToken)
            } else {
                TogeResult.Success(it)
            }
        }

    override suspend fun getNewUserInfo(): TogeResult<UserInfo> =
        userDataSource.getUserInfo().onSuccess {
            val userInfo = it.userInfo
            localPreference.run {
                uid = userInfo.userId
                nickname = userInfo.nickname
                profileUrl = userInfo.profileImageUrl
            }
        }.map {
            it.userInfo.toUserInfo()
        }

    override suspend fun getUserInfo(): TogeResult<UserInfo> {
        return TogeResult.Success(
            UserInfo(
                userId = localPreference.uid,
                nickname = localPreference.nickname,
                profileImageUrl = BuildConfig.RES_URL + localPreference.profileUrl
            )
        )
    }

    override suspend fun getUserInfoFlow(): Flow<TogeResult<UserInfo>> {
        getNewUserInfo()
        return combine(
            localPreference.uidFlow,
            localPreference.nicknameFlow,
            localPreference.profileUrlFlow
        ) { uid, nickname, profileImage ->
            val userInfo = UserInfo(
                userId = uid,
                nickname = nickname,
                profileImageUrl = BuildConfig.RES_URL + profileImage,
            )

            if (uid.isNotEmpty()) {
                TogeResult.Success(userInfo)
            } else {
                TogeResult.Error(togeError = InvalidUserInfo)
            }
        }
    }

    override suspend fun logout(): TogeResult<Boolean> {
        localPreference.run {
            loginState = LoginState.Logout.name
            clear()
        }
        return TogeResult.Success(true)
    }

    override suspend fun getLoginStateFlow(): Flow<TogeResult<LoginState>> =
        localPreference.loginStateFlow.map {
            TogeResult.Success(LoginState.getLoginState(it))
        }

    override suspend fun updateAccessToken(): TogeResult<Boolean> {
        return userDataSource.refreshingAccessToken().onSuccess {
            localPreference.run {
                parseJwtPayload(it.accessToken ?: "")?.let { payload ->
                    uid = payload.getString("userId") ?: ""
                    nickname = payload.getString("nickname") ?: ""
                    profileUrl = payload.getString("profileUrl") ?: ""
                    accessTokenIat = payload.getLong("iat")
                }
                accessToken = it.accessToken ?: ""
            }
        }.map { it.toSuccessBoolean() }
    }

    override suspend fun modifyProfileImage(): TogeResult<Boolean> =
        userDataSource.modifyProfileImage().onSuccess {
            localPreference.profileUrl = it.userInfo.profileImageUrl
        }.map { it.toSuccessBoolean() }


    private fun parseJwtPayload(jwt: String): JSONObject? {
        return try {
            val parts = jwt.split(".")
            if (parts.size != 3) return null

            val payload = parts[1]
            val decodedBytes =
                Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val decodedString = String(decodedBytes, Charsets.UTF_8)
            JSONObject(decodedString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
