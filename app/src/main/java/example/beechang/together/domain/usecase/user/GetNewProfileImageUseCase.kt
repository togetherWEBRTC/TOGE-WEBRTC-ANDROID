package example.beechang.together.domain.usecase.user

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.UserRepository
import javax.inject.Inject

class GetNewProfileImageUseCase @Inject constructor(
    private val userRepository: UserRepository,
){
    suspend operator fun invoke(): TogeResult<Boolean> {
        return userRepository.modifyProfileImage()
    }
}