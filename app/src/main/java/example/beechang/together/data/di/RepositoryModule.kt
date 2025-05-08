package example.beechang.together.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import example.beechang.together.data.repository.RoomRepositoryImpl
import example.beechang.together.data.repository.SignallingRepositoryImpl
import example.beechang.together.data.repository.UserRepositoryImpl
import example.beechang.together.domain.repository.RoomRepository
import example.beechang.together.domain.repository.SignallingRepository
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindsUserRepository(
        userRepository: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindsRoomRepository(
        roomRepository: RoomRepositoryImpl
    ): RoomRepository

    @Binds
    @Singleton
    abstract fun bindsSignallingRepository(
        signallingRepository: SignallingRepositoryImpl
    ): SignallingRepository
}