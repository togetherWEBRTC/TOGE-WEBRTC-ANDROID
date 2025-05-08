package example.beechang.together.data.websocket

import android.util.Log
import example.beechang.together.BuildConfig
import example.beechang.together.data.response.SocketEventConstants
import example.beechang.together.data.response.WebSocketEventResponse
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.json.JSONObject
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException
import kotlin.reflect.KClass


@Singleton
class SocketIOWebSocketClient @Inject constructor() : WebSocketClient, CoroutineScope {

    private var socket: Socket? = null

    override var isConnected = false

    private var currentToken: String? = null

    private val _eventFlow = MutableSharedFlow<WebSocketEventResponse>()
    override val eventFlow: SharedFlow<WebSocketEventResponse> = _eventFlow

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
            val resultDeferred = async { getConnectionResult() }
            socket.connect()

            val result = resultDeferred.await().isSuccess
            isConnected = result

            if (result) {
                socket.off()
                listeningEvent()
            }
            return result
        } catch (e: Exception) {
            isConnected = false
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
                    isConnected = false
                    result
                } else {
                    true
                }
            } ?: run {
                true
            }
        } catch (e: Exception) {
            isConnected = false
            Log.e("SocketIOWebSocketClient", "Error disconnecting socket: ${e.message}")
            e.printStackTrace()
            false
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

    override suspend fun <RESP : Any> emitWithAck(
        event: String,
        responseType: KClass<RESP>
    ): TogeResult<RESP> {
        return emitWithAck(event, Unit, responseType)
    }

    override suspend fun <REQ : Any, RESP : Any> emitWithAck(
        event: String,
        request: REQ,
        responseType: KClass<RESP>
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

    @OptIn(InternalSerializationApi::class)
    private fun <RESP : Any> parseAckResponse(
        args: Array<Any?>,
        responseType: KClass<RESP>
    ): RESP {
        val raw = args.firstOrNull()
        val jsonObj = when (raw) {
            is JSONObject -> raw
            is String -> JSONObject(raw)
            else -> throw IllegalArgumentException("Invalid response format: ${args.contentToString()}")
        }
        return json.decodeFromString(responseType.serializer(), jsonObj.toString())
    }


    private suspend fun getResultEventDisconnect(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        var listenerRef: Emitter.Listener? = null
        val listener = Emitter.Listener {
            deferred.complete(true)
            socket?.off(Socket.EVENT_DISCONNECT, listenerRef)
        }
        listenerRef = listener
        socket?.on(Socket.EVENT_DISCONNECT, listener)
        return deferred.await()
    }

    private suspend fun getConnectionResult(): Result<Boolean> {
        val deferred = CompletableDeferred<Result<Boolean>>()

        var connectListenerRef: Emitter.Listener? = null
        var authErrorListenerRef: Emitter.Listener? = null
        var connectErrorListenerRef: Emitter.Listener? = null

        fun cleanupAllListeners() {
            socket?.apply {
                connectListenerRef?.let { off(Socket.EVENT_CONNECT, it) }
                authErrorListenerRef?.let { off(SocketEventConstants.AUTH_ERROR, it) }
                connectErrorListenerRef?.let { off(Socket.EVENT_CONNECT_ERROR, it) }
            }
        }

        val connectListener = Emitter.Listener { _ ->
            isConnected = true
            cleanupAllListeners()
            deferred.complete(Result.success(true))
        }

        connectListenerRef = connectListener
        val authErrorListener = Emitter.Listener { args ->
            val error = args.firstOrNull()?.toString() ?: "Unknown auth error"
            cleanupAllListeners()
            deferred.complete(Result.failure(Exception("Authentication error: $error")))
        }
        authErrorListenerRef = authErrorListener

        val connectErrorListener = Emitter.Listener { args ->
            val error = args.firstOrNull()?.toString() ?: "Unknown connection error"
            cleanupAllListeners()
            deferred.complete(Result.failure(Exception("Connection error: $error")))
        }
        connectErrorListenerRef = connectErrorListener

        socket?.apply {
            on(Socket.EVENT_CONNECT, connectListener)
            on(SocketEventConstants.AUTH_ERROR, authErrorListener)
            on(Socket.EVENT_CONNECT_ERROR, connectErrorListener)
        }

        try {
            return deferred.await()
        } catch (e: Exception) {
            cleanupAllListeners()
            throw e
        }
    }

    private fun initializeSocket(token: String) {
        try {
            val options = IO.Options().apply {
                auth = mapOf("accessToken" to token)
                reconnection = true
                reconnectionDelay = 1000
                reconnectionAttempts = 3
            }
            socket?.off()
            socket = IO.socket(BuildConfig.WEBSOCKET_URL, options)
            currentToken = token
        } catch (e: Exception) {
            throw e
        }
    }
}
