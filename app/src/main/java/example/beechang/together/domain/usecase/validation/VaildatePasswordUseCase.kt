package example.beechang.together.domain.usecase.validation

import example.beechang.together.domain.data.TogeResult
import javax.inject.Inject

class VaildatePasswordUseCase @Inject constructor() {

    suspend operator fun invoke(password: String): TogeResult<Boolean> {
        return if (password.length in 2..12) {
            TogeResult.Success(true)
        } else {
            TogeResult.Error(msg = "")
        }
    }
}