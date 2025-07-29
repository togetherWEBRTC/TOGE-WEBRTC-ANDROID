package example.beechang.together

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TogeAppEventViewModel @Inject constructor() : ViewModel() {

    private val _eventFlow = MutableSharedFlow<TogeAppEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onStopCallReceived() {
        viewModelScope.launch {
            _eventFlow.emit(TogeAppEvent.StopCalling)
        }
    }
}

sealed interface TogeAppEvent {
    object StopCalling : TogeAppEvent
}