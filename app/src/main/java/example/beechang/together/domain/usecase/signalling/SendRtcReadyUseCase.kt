package example.beechang.together.domain.usecase.signalling

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.SignallingRepository
import javax.inject.Inject

class SendRtcReadyUseCase @Inject constructor(
    private val signallingRepository: SignallingRepository
) {
    suspend operator fun invoke(roomCode: String): TogeResult<Boolean> =
        signallingRepository.sendRtcReady(roomCode = roomCode)
}