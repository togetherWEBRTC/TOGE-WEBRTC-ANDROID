package example.beechang.together.data.websocket

import android.util.Log
import example.beechang.together.BuildConfig
import example.beechang.together.data.response.SocketEventConstants
import example.beechang.together.data.response.WebSocketEventResponse
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.json.JSONObject
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Singleton
class SocketIOWebSocketClient @Inject constructor() : WebSocketClient, CoroutineScope {

    private var socket: Socket? = null

    override var isConnected: Boolean = false
        get() = _connectionStateFlow.value == WebSocketConnectionState.CONNECTED

    private var currentToken: String? = null

    private val _eventFlow = MutableSharedFlow<WebSocketEventResponse>(
        replay = 0,
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val eventFlow: SharedFlow<WebSocketEventResponse> = _eventFlow

    private val _connectionStateFlow = MutableStateFlow(WebSocketConnectionState.PENDING)
    override val connectionStateFlow: Flow<WebSocketConnectionState> = _connectionStateFlow

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext = job + Dispatchers.IO

    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        prettyPrint = false
        encodeDefaults = true
    }

    override suspend fun connect(token: String): Boolean {
        try {
            if (isConnected) {
                disconnect()
            }

            initializeSocket(token)
            val socket = socket ?: return false

            val isSuccessSocketConnection = async { isSuccessSocketConnection() }
            launch {
                setSocketConnectionState()
                listeningEvent()
            }
            socket.connect()

            return isSuccessSocketConnection.await()
        } catch (e: Exception) {
            _connectionStateFlow.update { WebSocketConnectionState.DISCONNECTED }
            Log.e("SocketIOWebSocketClient", "Error connecting socket: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    override suspend fun disconnect(): Boolean {
        return try {
            socket?.let {
                if (isConnected) {
                    val disconnectDeferred = async { getResultEventDisconnect() }
                    it.disconnect()
                    val result = disconnectDeferred.await()
                    it.off()
                    if (result) {
                        _connectionStateFlow.update { WebSocketConnectionState.DISCONNECTED }
                    }
                    result
                } else {
                    true
                }
            } ?: run {
                true
            }
        } catch (e: Exception) {
            _connectionStateFlow.update { WebSocketConnectionState.DISCONNECTED }
            Log.e("SocketIOWebSocketClient", "Error disconnecting socket: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    override suspend fun <RESP : Any> emitWithAck(
        event: String,
        responseType: KSerializer<RESP>
    ): TogeResult<RESP> {
        return emitWithAck(event, Unit, responseType)
    }

    override suspend fun <REQ : Any, RESP : Any> emitWithAck(
        event: String,
        request: REQ,
        responseType: KSerializer<RESP>
    ): TogeResult<RESP> {
        if (!isConnected || socket == null) {
            val msg = "Need to connect socket first"
            return TogeResult.Error(TogeError.NetworkError(msg), msg)
        }

        val reqJson = serializeRequest(request)

        return try {
            val args = performEmitWithAck(socket!!, event, reqJson)
            val respObj = parseAckResponse(args, responseType)
            TogeResult.Success(respObj)
        } catch (e: SocketTimeoutException) {
            TogeResult.Error(TogeError.DataError("Socket timeout"), "Socket timeout")
        } catch (e: IllegalArgumentException) {
            TogeResult.Error(TogeError.InvalidParams(e.message), e.message)
        } catch (e: Exception) {
            TogeResult.Error(TogeError.UnknownError(e.message), e.message)
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun <REQ : Any> serializeRequest(request: REQ): JSONObject {
        return when (request) {
            is Unit -> JSONObject()
            else -> {
                val serializer = serializer(request::class.java)
                val jsonString = json.encodeToString(serializer, request)
                JSONObject(jsonString)
            }
        }
    }

    private suspend fun performEmitWithAck(
        socket: Socket,
        event: String,
        requestJson: JSONObject,
        timeoutMs: Long = 10_000L
    ): Array<Any?> = try {
        withTimeout(timeoutMs) {
            suspendCancellableCoroutine<Array<Any?>> { cont ->
                socket.emit(event, requestJson, Ack { args ->
                    cont.resume(args) { err, _, _ -> cont.resumeWithException(err) }
                })
            }
        }
    } catch (e: TimeoutCancellationException) {
        throw SocketTimeoutException("Socket timeout after ${timeoutMs}ms")
    }

    private fun <RESP : Any> parseAckResponse(
        args: Array<Any?>,
        responseType: KSerializer<RESP>
    ): RESP {
        val raw = args.firstOrNull()
        val jsonObj = when (raw) {
            is JSONObject -> raw
            is String -> JSONObject(raw)
            else -> throw IllegalArgumentException("Invalid response format: ${args.contentToString()}")
        }
        return json.decodeFromString(responseType, jsonObj.toString())
    }


    private suspend fun getResultEventDisconnect(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        val listener = Emitter.Listener {
            deferred.complete(true)
        }
        socket?.on(Socket.EVENT_DISCONNECT, listener)
        return deferred.await()
    }

    private fun initializeSocket(token: String) {
        try {
            val options = IO.Options().apply {
                auth = mapOf("accessToken" to token)
                reconnection = true
                randomizationFactor = RANDOMIZATION_FACTOR
                reconnectionDelay = RECONNECTION_DELAY
                reconnectionAttempts = RECONNECTION_ATTEMPTS
            }
            socket?.off()
            socket = IO.socket(BuildConfig.WEBSOCKET_URL, options)
            currentToken = token
        } catch (e: Exception) {
            throw e
        }
    }

    private suspend fun isSuccessSocketConnection(): Boolean = suspendCancellableCoroutine { cont ->
        socket?.let {
            it.run {
                on(Socket.EVENT_CONNECT) {
                    _connectionStateFlow.update { WebSocketConnectionState.CONNECTED }
                    cont.resume(true)
                }
                on(Socket.EVENT_CONNECT_ERROR) {
                    _connectionStateFlow.update { WebSocketConnectionState.DISCONNECTED }
                    cont.resume(false)
                }
                on(Socket.EVENT_DISCONNECT) {
                    _connectionStateFlow.update { WebSocketConnectionState.DISCONNECTED }
                    cont.resume(false)
                }
            }
        }
    }

    private fun setSocketConnectionState() {
        socket?.let {
            it.run {
                on(Manager.EVENT_RECONNECT) {
                    _connectionStateFlow.update { WebSocketConnectionState.RECONNECTED }
                    _connectionStateFlow.update { WebSocketConnectionState.CONNECTED }
                }
                on(Manager.EVENT_RECONNECT_ATTEMPT) {
                    _connectionStateFlow.update { WebSocketConnectionState.RECONNECTING }
                }
                on(Manager.EVENT_RECONNECT_FAILED) {
                    _connectionStateFlow.update { WebSocketConnectionState.FAILED_RECONNECT }
                    _connectionStateFlow.update { WebSocketConnectionState.DISCONNECTED }
                }
            }
        }
    }

    private fun listeningEvent() {
        socket?.let { socket ->
            SocketEventConstants.INCOMING_EVENTS.forEach { eventName ->
                socket.on(eventName) { args ->
                    launch {
                        val data = args.firstOrNull()
                        val jsonData = when {
                            data == null -> null
                            data is String && data.startsWith("{") -> data
                            else -> {
                                try {
                                    JSONObject(data.toString()).toString()
                                } catch (e: Exception) {
                                    """{"data":"${data.toString().replace("\"", "\\\"")}"}"""
                                }
                            }
                        }
                        Log.d("SocketIOWebSocketClient", "Event: $eventName, Data: $jsonData")
                        _eventFlow.emit(WebSocketEventResponse(eventName, jsonData))
                    }
                }
            }
        }
    }

    companion object {
        const val RANDOMIZATION_FACTOR = 0.7 // disconnet 0~1700ms / 0~3400ms / 0~6800ms
        const val RECONNECTION_DELAY = 1000L
        const val RECONNECTION_ATTEMPTS = 5
    }
}
