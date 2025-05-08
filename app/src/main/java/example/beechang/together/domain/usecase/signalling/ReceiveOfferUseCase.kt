package example.beechang.together.domain.usecase.signalling

import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.RoomSdp
import example.beechang.together.domain.repository.SignallingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReceiveOfferUseCase @Inject constructor(
    private val signallingRepository: SignallingRepository
) {
    suspend operator fun invoke(): Flow<TogeResult<RoomSdp>> =
        signallingRepository.receiveOffer()
}