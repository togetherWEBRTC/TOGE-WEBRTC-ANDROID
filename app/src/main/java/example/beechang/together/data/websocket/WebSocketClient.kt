package example.beechang.together.data.websocket

import example.beechang.together.data.response.WebSocketEventResponse
import example.beechang.together.domain.data.TogeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.KSerializer

interface WebSocketClient {
    var isConnected: Boolean
    val eventFlow: Flow<WebSocketEventResponse>
    suspend fun connect(token: String): Boolean
    suspend fun disconnect(): Boolean
    suspend fun <RESP : Any> emitWithAck(
        event: String,
        responseType: KSerializer<RESP>
    ): TogeResult<RESP>

    suspend fun <REQ : Any, RESP : Any> emitWithAck(
        event: String,
        request: REQ,
        responseType: KSerializer<RESP>
    ): TogeResult<RESP>
}

