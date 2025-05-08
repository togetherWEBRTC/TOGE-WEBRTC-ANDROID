package example.beechang.together.domain.usecase.signalling

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.repository.SignallingRepository
import javax.inject.Inject

class SendOfferUseCase @Inject constructor(
    private val signallingRepository: SignallingRepository
) {
    suspend operator fun invoke(
        roomCode: String,
        toUserId: String,
        sdp: String
    ): TogeResult<Boolean> =
        signallingRepository.sendOffer(
            roomCode = roomCode,
            toUserId = toUserId,
            sdp = sdp
        )
}