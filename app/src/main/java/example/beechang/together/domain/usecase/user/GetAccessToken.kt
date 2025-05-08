package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAccessToken @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Flow<TogeResult<String>> =
        userRepository.getLocalAccessTokenFlow()
}