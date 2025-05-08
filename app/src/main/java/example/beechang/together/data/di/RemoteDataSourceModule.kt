package example.beechang.together.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import example.beechang.together.data.http.api.UserDataSource
import example.beechang.together.data.http.api.UserDataSourceImpl
import example.beechang.together.data.websocket.RoomDataSourceImpl
import example.beechang.together.data.websocket.SignallingDataSourceImpl
import example.beechang.together.data.websocket.RoomDataSource
import example.beechang.together.data.websocket.SignallingDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RemoteDataSourceModule {

    @Binds
    @Singleton
    abstract fun bindUserDataSource(
        userDataSourceImpl: UserDataSourceImpl
    ): UserDataSource

    @Binds
    @Singleton
    abstract fun bindRoomDataSource(
        roomDataSourceImpl: RoomDataSourceImpl
    ): RoomDataSource

    @Binds
    @Singleton
    abstract fun bindSignallingDataSource(
        signallingDataSourceImpl: SignallingDataSourceImpl
    ): SignallingDataSource
}