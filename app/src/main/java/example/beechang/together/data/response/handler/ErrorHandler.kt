package example.beechang.together.data.response.handler

import android.util.Log
import example.beechang.together.data.response.TogeResponse
import example.beechang.together.data.websocket.WebSocketClient
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import retrofit2.Response
import kotlin.reflect.KClass

fun serverErrorCodeToTogeError(code: Int, msg: String? = null): TogeError {
    return when (code) {
        1 -> TogeError.ServerError(msg)
        2 -> TogeError.InvalidParams(msg)
        3 -> TogeError.DataError(msg)

        1001 -> TogeError.InvalidAccessToken
        1002 -> TogeError.InvalidRefreshToken
        1003 -> TogeError.FailedLogin
        1004 -> TogeError.DuplicatedId
        1005 -> TogeError.PasswordNotMatch

        10001 -> TogeError.AlreadyJoinedRoom
        10002 -> TogeError.RoomNotFound
        10003 -> TogeError.NotRoomOwner
        10004 -> TogeError.NotRoomMember
        10005 -> TogeError.RequestedSameState
        10006 -> TogeError.AlreadyExistedRoom

        else -> TogeError.UnknownError(msg)
    }
}

/**
 * [사용 전]
 * ```
 * try {
 *     val response = apiService.getData(id)
 *     if (response.isSuccessful) {
 *         val body = response.body()
 *         if (body != null) {
 *             if (body.code == 0) {
 *                 // 성공 처리
 *             } else {
 *                 // 서버 오류 처리
 *             }
 *         } else {
 *             // body null 처리
 *         }
 *     } else {
 *         // API 응답 실패 처리
 *     }
 * } catch (e: Exception) {
 *     // 예외 처리
 * }
 * ```
 * [사용 후]
 * ```
 * apiToResult { apiService.getData(id) }
 * ```
 *
 * @return TogeResult<T> API 호출 결과를 래핑한 객체
 */
suspend fun <T : TogeResponse> apiToResult(
    call: suspend () -> Response<T>
): TogeResult<T> = try {
    val response = call()

    if (response.isSuccessful) {
        response.body()?.let {
            if (it.code == 0) {
                TogeResult.Success(it)
            } else {
                TogeResult.Error(
                    togeError = serverErrorCodeToTogeError(it.code, it.message),
                    msg = it.message
                )
            }
        } ?: run {
            TogeResult.Error(
                togeError = TogeError.UnknownError("body is null"),
                msg = "body is null"
            )
        }
    } else {
        TogeResult.Error(
            togeError = TogeError.NetworkError(response.message()),
            msg = response.errorBody()?.string(),
        )
    }
} catch (e: Exception) {
    Log.e("API_ERROR", "API ERROR : ${e.message.toString()}")
    TogeResult.Error(
        togeError = TogeError.NetworkError(e.message),
        exception = e
    )
}


suspend fun <T : TogeResponse> togeToResult(
    result: suspend () -> TogeResult<T>
): TogeResult<T> = try {
    val response = result()
    when (response) {
        is TogeResult.Success -> {
            if (response.data.code == 0) {
                response
            } else {
                TogeResult.Error(
                    togeError = serverErrorCodeToTogeError(
                        response.data.code,
                        response.data.message
                    ),
                    msg = response.data.message
                )
            }
        }

        is TogeResult.Error -> response
    }
} catch (e: Exception) {
    Log.e("TOGE_API_ERROR", "TOGE_API_ERROR : ", e)
    TogeResult.Error(
        togeError = TogeError.NetworkError(e.message),
        exception = e
    )
}

val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    prettyPrint = false
    encodeDefaults = true
}

@OptIn(InternalSerializationApi::class)
fun <T : Any> socketEventToResultFlow(
    webSocketClient: WebSocketClient,
    eventName: String,
    resType: KClass<T>
): Flow<TogeResult<T>> {
    return webSocketClient.eventFlow
        .filter { it.event == eventName }
        .map { response ->
            try {
                response.jsonData?.let { jsonData ->
                    val parsedResponse = json.decodeFromString(resType.serializer(), jsonData)
                    TogeResult.Success(parsedResponse)
                } ?: TogeResult.Error(
                    togeError = TogeError.DataError(msg = "Response body is null"),
                    msg = "Response body is null"
                )
            } catch (e: Exception) {
                Log.e("SOCKET_EVENT_ERROR", "Error parsing $eventName: ${e.message}", e)
                TogeResult.Error(
                    togeError = TogeError.DataError("Failed to parse $eventName: ${e.message}"),
                    msg = e.message,
                    exception = e
                )
            }
        }
        .catch { e ->
            emit(
                TogeResult.Error(
                    togeError = TogeError.UnknownError("Error processing $eventName: ${e.message}"),
                    msg = e.message,
                    exception = e as Exception?
                )
            )
        }
        .flowOn(Dispatchers.IO)
}