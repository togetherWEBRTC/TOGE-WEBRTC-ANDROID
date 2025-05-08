package example.beechang.together.domain.model


enum class LoginState {
    Login, SessionExpired, Logout, None;

    companion object {
        fun getLoginState(state: String): LoginState {
            return when (state) {
                "Login" -> Login
                "SessionExpired" -> SessionExpired
                "Logout" -> Logout
                "None" -> None
                else -> None
            }
        }
    }
}