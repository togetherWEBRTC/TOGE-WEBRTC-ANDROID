package example.beechang.together.data.di

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @ApplicationScope
    @Singleton
    @Provides
    fun provideApplicationScope(): CoroutineScope {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e("ApplicationScope", "Unhandled exception", throwable)
        }
        return CoroutineScope(SupervisorJob() + Dispatchers.Default + exceptionHandler)
    }
}