package example.beechang.together.data.response

object SocketEventConstants {

    // 연결 관련
    const val CONNECT = "connection"
    const val DISCONNECT = "disconnect"
    const val CONNECT_ERROR = "connect_error"
    const val AUTH_ERROR = "auth_error"

    // 방 관련
    const val ROOM_CREATE = "room_create"
    const val ROOM_LEAVE = "room_leave"
    const val ROOM_REQUEST_JOIN = "room_request_join"
    const val ROOM_REQUEST_JOIN_CANCEL = "room_request_join_cancel"
    const val ROOM_NOTIFY_WAIT = "room_notify_wait"
    const val ROOM_DECIDE_JOIN_FROM_HOST = "room_decide_join_from_host"
    const val ROOM_NOTIFY_DECIDE_JOIN_FROM_HOST = "room_notify_decide_join_host"
    const val ROOM_NOTIFY_UPDATE_PARTICIPANT = "room_notify_update_participant"
    const val ROOM_NOTIFY_UPDATE_OWNER = "room_notify_update_owner"
    const val ROOM_MEMBER_LIST = "room_member_list"
    const val ROOM_MEMBER_EXPEL = "room_member_expel"
    const val ROOM_NOTIFY_EXPEL = "room_notify_expel"

    // 시그널링 관련
    const val SIGNAL_SEND_OFFER = "signal_send_offer"
    const val SIGNAL_NOTIFY_OFFER = "signal_notify_offer"
    const val SIGNAL_SEND_ANSWER = "signal_send_answer"
    const val SIGNAL_NOTIFY_ANSWER = "signal_notify_answer"
    const val SIGNAL_SEND_ICE = "signal_send_ice"
    const val SIGNAL_NOTIFY_ICE = "signal_notify_ice"
    const val RTC_READY = "rtc_ready"

    // 통화 관련
    const val CALL_CHANGE_MIC = "call_change_mic"
    const val CALL_NOTIFY_CHANGE_MIC = "call_notify_change_mic"
    const val CALL_CHANGE_CAMERA = "call_change_camera"
    const val CALL_NOTIFY_CHANGE_CAMERA = "call_notify_change_camera"
    const val CALL_CHANGE_HAND_RAISED = "call_change_hand_raised"
    const val CALL_NOTIFY_CHANGE_HAND_RAISED = "call_notify_change_hand_raised"

    // 채팅 관련
    const val CHAT_SEND_CHAT_MESSAGE = "chat_send_message"
    const val CHAT_NOTIFY_CHAT_MESSAGE = "chat_notify_message"

    // 에러
    const val SOCKET_ERROR = "socket_error"

    val CONNECTION_EVENTS = setOf(
        CONNECT, DISCONNECT, AUTH_ERROR , CONNECT_ERROR
    )

    val ROOM_EVENTS = setOf(
        ROOM_CREATE,
        ROOM_LEAVE,
        ROOM_REQUEST_JOIN,
        ROOM_REQUEST_JOIN_CANCEL,
        ROOM_NOTIFY_WAIT,
        ROOM_DECIDE_JOIN_FROM_HOST,
        ROOM_NOTIFY_DECIDE_JOIN_FROM_HOST,
        ROOM_NOTIFY_UPDATE_PARTICIPANT,
        ROOM_NOTIFY_UPDATE_OWNER,
        ROOM_MEMBER_LIST,
        ROOM_MEMBER_EXPEL,
        ROOM_NOTIFY_EXPEL
    )

    val SIGNAL_EVENTS = setOf(
        SIGNAL_SEND_OFFER,
        SIGNAL_NOTIFY_OFFER,
        SIGNAL_SEND_ANSWER,
        SIGNAL_NOTIFY_ANSWER,
        SIGNAL_SEND_ICE,
        SIGNAL_NOTIFY_ICE,
        RTC_READY
    )

    val CALL_EVENTS = setOf(
        CALL_CHANGE_MIC,
        CALL_NOTIFY_CHANGE_MIC,
        CALL_CHANGE_CAMERA,
        CALL_NOTIFY_CHANGE_CAMERA,
        CALL_CHANGE_HAND_RAISED,
        CALL_NOTIFY_CHANGE_HAND_RAISED
    )

    val CHAT_EVENTS = setOf(
        CHAT_SEND_CHAT_MESSAGE,
        CHAT_NOTIFY_CHAT_MESSAGE
    )

    val ERROR_EVENTS = setOf(
        SOCKET_ERROR
    )

    val ALL_EVENTS =
        CONNECTION_EVENTS + ROOM_EVENTS + SIGNAL_EVENTS + CALL_EVENTS + CHAT_EVENTS + ERROR_EVENTS

    val EMIT_EVENTS = setOf(
        ROOM_CREATE,
        ROOM_LEAVE,
        ROOM_REQUEST_JOIN,
        ROOM_REQUEST_JOIN_CANCEL,
        ROOM_DECIDE_JOIN_FROM_HOST,
        ROOM_MEMBER_EXPEL,
        SIGNAL_SEND_OFFER,
        SIGNAL_SEND_ANSWER,
        SIGNAL_SEND_ICE,
        CALL_CHANGE_MIC,
        CALL_CHANGE_CAMERA,
        CALL_CHANGE_HAND_RAISED,
        CHAT_SEND_CHAT_MESSAGE
    )

    val INCOMING_EVENTS = setOf(
        AUTH_ERROR,
        ROOM_NOTIFY_WAIT,
        ROOM_NOTIFY_DECIDE_JOIN_FROM_HOST,
        ROOM_NOTIFY_UPDATE_PARTICIPANT,
        ROOM_NOTIFY_UPDATE_OWNER,
        ROOM_MEMBER_LIST,
        ROOM_NOTIFY_EXPEL,
        SIGNAL_NOTIFY_OFFER,
        SIGNAL_NOTIFY_ANSWER,
        SIGNAL_NOTIFY_ICE,
        RTC_READY,
        CALL_NOTIFY_CHANGE_MIC,
        CALL_NOTIFY_CHANGE_CAMERA,
        CALL_NOTIFY_CHANGE_HAND_RAISED,
        CHAT_NOTIFY_CHAT_MESSAGE,
        SOCKET_ERROR
    )
}