package example.beechang.together.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import example.beechang.together.BuildConfig
import example.beechang.together.data.local.DataStoreLocalPreference
import example.beechang.together.domain.data.LocalPreference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class LocalDataSourceModule {

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile(BuildConfig.LOCAL_PREF) },
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class LocalPreferenceModule {

    @Binds
    @Singleton
    abstract fun bindLocalPreference(
        dataStoreLocalPreference: DataStoreLocalPreference
    ): LocalPreference
}