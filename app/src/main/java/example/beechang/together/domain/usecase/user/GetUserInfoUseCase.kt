package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.UserInfo
import example.beechang.together.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend fun observeUserInfo(): Flow<TogeResult<UserInfo>> =
        userRepository.getUserInfoFlow()

    suspend fun getNewFreshUserInfo(): TogeResult<UserInfo> =
        userRepository.getNewUserInfo()

    suspend fun getUserInfo(): TogeResult<UserInfo> =
        userRepository.getUserInfo()
}