package example.beechang.together.data.repository

import example.beechang.together.data.websocket.RoomDataSource
import example.beechang.together.domain.data.LocalPreference
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.data.map
import example.beechang.together.domain.data.mapSuccessOrProvideError
import example.beechang.together.domain.data.mapToge
import example.beechang.together.domain.model.RoomCode
import example.beechang.together.domain.model.RoomParticipant
import example.beechang.together.domain.model.RoomWaitingMembers
import example.beechang.together.domain.model.UpdatedRoomParticipant
import example.beechang.together.domain.repository.RoomRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepositoryImpl @Inject constructor(
    private val roomDataSource: RoomDataSource,
    private val localPreference: LocalPreference
) : RoomRepository {

    override suspend fun connect(): TogeResult<Boolean> =
        roomDataSource.connect(localPreference.accessToken)


    override suspend fun disconnect(): TogeResult<Boolean> =
        roomDataSource.disconnect()

    override suspend fun createRoom(roodCode: String?): TogeResult<RoomCode> =
        roomDataSource.createRoom(roomCode = roodCode ?: "")
            .mapSuccessOrProvideError(errorType = TogeError.FailedToCreateRoom) {
                it.toRoomCode()
            }

    override suspend fun requestWaitingEnter(roomCode: String): TogeResult<Boolean> =
        roomDataSource.requestWaitingEnter(roomCode = roomCode).map { it.toSuccessBoolean() }

    override suspend fun requestDecisionWaitingEnter(
        roomCode: String,
        targetUserId: String,
        isApprove: Boolean
    ): TogeResult<Boolean> =
        roomDataSource.requestDecisionWaitingEnter(
            roomCode = roomCode,
            targetUserId = targetUserId,
            isApprove = isApprove
        ).map { it.toSuccessBoolean() }

    override suspend fun getRoomParticipant(
        roomCode: String,
        isIncludingMySelf: Boolean
    ): TogeResult<List<RoomParticipant>> =
        roomDataSource.getRoomParticipant(
            roomCode = roomCode,
            isIncludingMySelf = isIncludingMySelf
        ).map { it.toRoomParticipant() }

    override suspend fun changeMicStatus(
        roomCode: String,
        isMicrophoneOn: Boolean
    ): TogeResult<Boolean> =
        roomDataSource.changeMicStatus(
            roomCode = roomCode,
            isMicrophoneOn = isMicrophoneOn
        ).map { it.toSuccessBoolean() }

    override suspend fun changeCameraStatus(
        roomCode: String,
        isCameraOn: Boolean
    ): TogeResult<Boolean> =
        roomDataSource.changeCameraStatus(
            roomCode = roomCode,
            isCameraOn = isCameraOn
        ).map { it.toSuccessBoolean() }

    override suspend fun receiveRoomNotifyWait(): Flow<TogeResult<RoomWaitingMembers>> {
        return roomDataSource.receiveRoomNotifyWaitingList()
            .mapToge { it.toRoomWaitingMembers() }
    }

    override suspend fun receiveRoomNotifyWaitingResult(): Flow<TogeResult<Boolean>> {
        return roomDataSource.receiveRoomResultWaiting()
            .mapToge { it.isApprove }
    }

    override suspend fun receiveRoomUpdatingParticipant(): Flow<TogeResult<UpdatedRoomParticipant>> {
        return roomDataSource.receiveRoomUpdatingParticipant()
            .mapToge { it.toUpdatedRoomParticipant() }
    }

    override suspend fun receiveRoomNotifyMicStatus(): Flow<TogeResult<RoomParticipant>> {
        return roomDataSource.receiveRoomNotifyMicStatus()
            .mapToge { it.toParticipant() }
    }

    override suspend fun receiveRoomNotifyCameraStatus(): Flow<TogeResult<RoomParticipant>> {
        return roomDataSource.receiveRoomNotifyCameraStatus()
            .mapToge { it.toParticipant() }
    }

}