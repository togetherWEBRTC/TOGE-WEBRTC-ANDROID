package example.beechang.together.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import example.beechang.together.data.http.interceptor.AuthInterceptor
import example.beechang.together.data.http.interceptor.LoggingInterceptor
import example.beechang.together.domain.data.LocalPreference
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
class OkHttpModule {

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): LoggingInterceptor = LoggingInterceptor()

    @Provides
    @Singleton
    fun provideAuthInterceptor(localPreference: LocalPreference): AuthInterceptor =
        AuthInterceptor(localPreference)

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        loggingInterceptor: LoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}