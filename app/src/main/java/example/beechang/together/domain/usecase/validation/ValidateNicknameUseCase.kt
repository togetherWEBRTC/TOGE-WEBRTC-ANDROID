package example.beechang.together.domain.usecase.validation

import example.beechang.together.domain.data.TogeResult
import javax.inject.Inject

class ValidateNicknameUseCase @Inject constructor(
) {
    suspend operator fun invoke(nickname: String): TogeResult<Boolean> {
        return if (nickname.length in 2..12) {
            TogeResult.Success(true)
        } else {
            TogeResult.Error(msg = "")
        }
    }
}