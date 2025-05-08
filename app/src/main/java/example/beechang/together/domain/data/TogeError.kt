package example.beechang.together.domain.data


sealed interface TogeError {
    data class NetworkError(val msg: String?) : TogeError
    data class ServerError(val msg: String?) : TogeError
    data class UnknownError(val msg: String?) : TogeError
    data class InvalidParams(val msg: String?) : TogeError
    data class DataError(val msg: String?) : TogeError

    object InvalidAccessToken : TogeError
    object InvalidRefreshToken : TogeError
    object InvalidUserInfo : TogeError

    object FailedLogin : TogeError
    object DuplicatedId : TogeError
    object PasswordNotMatch : TogeError
    object AlreadyJoinedRoom : TogeError

    object UnknownErrorFailedToCalling : TogeError
    object FailedToCreateRoom : TogeError
    object RoomNotFound : TogeError
    object NotRoomOwner : TogeError
    object NotRoomMember : TogeError
    object AlreadyExistedRoom : TogeError
    object RequestedSameState : TogeError
    object FailedToConnectRoom : TogeError
}

