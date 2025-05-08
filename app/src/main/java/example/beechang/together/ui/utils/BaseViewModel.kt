package example.beechang.together.ui.utils

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import example.beechang.together.domain.data.TogeError
import example.beechang.together.domain.data.TogeResult
import example.beechang.together.domain.model.LoginState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>(
    protected val savedStateHandle: SavedStateHandle,
    initState: State,
    private val stateKey: String = "viewModelState"
) : ViewModel() {

    private val _uiState = MutableStateFlow(savedStateHandle.get<State>(stateKey) ?: initState)
    protected val currentState: State
        get() = _uiState.value
    val uiState: StateFlow<State> = _uiState

    private val _sideEffect = MutableSharedFlow<Effect>()
    val sideEffect: SharedFlow<Effect> = _sideEffect

    private val _errorEffect = MutableSharedFlow<TogeError>()
    val errorEffect: SharedFlow<TogeError> = _errorEffect

    private val _loginState = MutableStateFlow<LoginState>(
        savedStateHandle.get<String>(stateKey + "login")?.let { LoginState.getLoginState(it) }
            ?: LoginState.None
    )
    val loginState: StateFlow<LoginState> = _loginState

    public abstract fun onEvent(event: Event)

    protected fun updateState(block: State.() -> State) {
        _uiState.value.block().run {
            _uiState.update { this }
            savedStateHandle[stateKey] = this
        }
    }

    protected fun sendEffect(effect: Effect) = viewModelScope.launch {
        _sideEffect.emit(effect)
    }

    protected fun sendError(togeError: TogeError) = viewModelScope.launch {
        _errorEffect.emit(togeError)
    }

    protected fun updateLoginState(loginState: LoginState) = viewModelScope.launch {
        _loginState.value = loginState
        savedStateHandle[stateKey + "login"] = loginState.name
    }

    protected fun handleError(
        togeError: TogeError?, message: String? = null, exception: Exception? = null
    ) = viewModelScope.launch {
        Log.e(
            "$stateKey : ViewModel",
            "TogeError : $togeError, message : $message, exception : $exception"
        )
        togeError?.let {
            _errorEffect.emit(it)
        } ?: run {
            _errorEffect.emit(TogeError.UnknownError(exception?.message))
        }
    }

    protected fun <T> Flow<TogeResult<T>>.collectInViewModel(
        onSuccess: (T) -> Unit,
        onFinally: () -> Unit = {},
    ) = viewModelScope.launch(Dispatchers.IO) {
        this@collectInViewModel
            .catch { e ->
                emit(
                    TogeResult.Error(
                        togeError = TogeError.UnknownError(e.message),
                        msg = e.message,
                        exception = e as? Exception ?: Exception(e)
                    )
                )
            }
            .collect { result ->
                when (result) {
                    is TogeResult.Success -> {
                        onSuccess(result.data)
                        onFinally()
                    }

                    is TogeResult.Error -> {
                        handleError(result.togeError, result.msg, result.exception)
                        onFinally()
                    }
                }
            }
    }

    protected fun <T> handleEvent(
        action: suspend () -> TogeResult<T>,
        onStart: (() -> Unit)? = null,
        onSuccess: suspend (T) -> Unit,
        onError: ((TogeResult.Error) -> Unit)? = null,
        handleCommonErrors: Boolean = true,
        onFinally: (() -> Unit)? = null
    ) = viewModelScope.launch() {
        onStart?.invoke()
        val result = action()
        when (result) {
            is TogeResult.Success -> onSuccess(result.data)
            is TogeResult.Error -> {
                onError?.invoke(result)
                if (handleCommonErrors) {
                    handleError(result.togeError, result.msg, result.exception)
                }
            }
        }
        onFinally?.invoke()
    }
}