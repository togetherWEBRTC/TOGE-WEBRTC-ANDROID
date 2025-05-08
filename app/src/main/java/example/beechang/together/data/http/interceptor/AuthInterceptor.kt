package example.beechang.together.data.http.interceptor

import android.util.Base64
import example.beechang.together.BuildConfig
import example.beechang.together.domain.data.LocalPreference
import example.beechang.together.domain.model.LoginState
import jakarta.inject.Inject
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject

class AuthInterceptor @Inject constructor(
    private val localPreference: LocalPreference
) : Interceptor {

    companion object {
        private const val FRESH_TOKEN_WINDOW_SECONDS =
            300 //토큰 갱신시간 해당초전인지 확인 후 연속적으로 갱신요청 시 재갱신 방지
        private val needRefreshTokenUrl = setOf<String>("/api/auth/refresh/token")
    }

    override fun intercept(chain: Interceptor.Chain): Response {

        val originalRequest = chain.request()
        if (localPreference.accessToken.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        val headerToken = if (needRefreshTokenUrl.contains(originalRequest.url.encodedPath)) {
            localPreference.refreshToken
        } else {
            localPreference.accessToken
        }

        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $headerToken")
            .build()
        var response = chain.proceed(newRequest)

        if (response.code == 403) {
            logoug()
            return createForbiddenResponse(originalRequest)
        }

        if (response.code == 401) {
            response.close()

            synchronized(this) {
                val currentTime = System.currentTimeMillis() / 1000
                val tokenIat = localPreference.accessTokenIat
                val timeSinceIssued = currentTime - tokenIat
                if (timeSinceIssued < FRESH_TOKEN_WINDOW_SECONDS) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${localPreference.accessToken}")
                        .build()
                    return chain.proceed(retryRequest)
                }

                if (tryRefreshToken(chain)) {
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer ${localPreference.accessToken}")
                        .build()
                    return chain.proceed(retryRequest)
                } else {
                    logoug()
                    return createForbiddenResponse(originalRequest)
                }
            }
        }

        return response
    }

    @Synchronized
    private fun tryRefreshToken(chain: Interceptor.Chain): Boolean {

        if (localPreference.refreshToken.isEmpty()) {
            return false
        }

        try {
            val refreshRequest = Request.Builder()
                .url(BuildConfig.API_URL + "api/auth/refresh/token")
                .header("Authorization", "Bearer ${localPreference.refreshToken}")
                .post(ByteArray(0).toRequestBody(null))
                .build()

            chain.proceed(refreshRequest).use { response ->
                if (response.code == 403 || !response.isSuccessful) {
                    return false
                }

                val responseBody = response.body?.string() ?: return false
                val jsonObject = JSONObject(responseBody)
                val newAccessToken = jsonObject.optString("accessToken")
                if (newAccessToken.isNotEmpty()) {
                    val iat = parseJwtPayload(newAccessToken)?.getLong("iat")
                        ?: (System.currentTimeMillis() / 1000)
                    localPreference.run {
                        accessToken = newAccessToken
                        accessTokenIat = iat
                    }
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return false
    }

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

    private fun createForbiddenResponse(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(403)
            .message("Forbidden")
            .body(ByteArray(0).toResponseBody(null))
            .build()
    }

    private fun logoug() {
        localPreference.logout(LoginState.SessionExpired.name)
        localPreference.clear()
    }

}