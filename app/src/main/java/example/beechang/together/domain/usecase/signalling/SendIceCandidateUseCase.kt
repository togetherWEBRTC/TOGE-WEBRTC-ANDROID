package example.beechang.together.domain.usecase.signalling

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.SignallingRepository
import javax.inject.Inject

class SendIceCandidateUseCase @Inject constructor(
    private val signallingRepository: SignallingRepository
) {
    suspend operator fun invoke(
        roomCode: String,
        toUserId: String,
        candidate: String,
        sdpMid: String,
        sdpMLineIndex: Int
    ): TogeResult<Boolean> = signallingRepository.sendIceCandidate(
        roomCode = roomCode,
        toUserId = toUserId,
        candidate = candidate,
        sdpMid = sdpMid,
        sdpMLineIndex = sdpMLineIndex
    )
}