package example.beechang.together.data.response

data class WebSocketEventResponse(
    val event: String, //SocketEventConstants
    val jsonData: String? = null
)