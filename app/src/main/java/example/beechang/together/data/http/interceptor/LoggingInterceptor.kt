package example.beechang.together.data.http.interceptor

import example.beechang.together.BuildConfig
import jakarta.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor

class LoggingInterceptor @Inject constructor() : Interceptor {
    private val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        return httpLoggingInterceptor.intercept(chain)
    }
}