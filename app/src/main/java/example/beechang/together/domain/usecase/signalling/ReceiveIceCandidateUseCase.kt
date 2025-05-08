package example.beechang.together.domain.usecase.signalling

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomIceCandidate
import example.beechang.together.domain.repository.SignallingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReceiveIceCandidateUseCase @Inject constructor(
    private val signallingRepository: SignallingRepository
) {
    suspend operator fun invoke(): Flow<TogeResult<RoomIceCandidate>> =
        signallingRepository.receiveIceCandidate()
}