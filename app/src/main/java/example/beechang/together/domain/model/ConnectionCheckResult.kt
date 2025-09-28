package example.beechang.together.domain.model

data class ConnectionCheckResult(
    val isDuplicateConnection: Boolean = false,
    val existingSocketId: String? = null,
    val currentSocketId: String? = null,
)