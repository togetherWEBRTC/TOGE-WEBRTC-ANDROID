package example.beechang.together.ui.call.waiting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import example.beechang.together.R
import example.beechang.together.domain.data.TogeError
import example.beechang.together.ui.call.CallNavDestination
import example.beechang.together.ui.component.dialog.TogeDialog
import example.beechang.together.ui.component.dialog.TogeOnlyConfirmBtnDialog
import example.beechang.together.ui.component.scaffold.TogeScaffold
import example.beechang.together.ui.component.snackbar.TogeSnackbarHost
import example.beechang.together.ui.component.topbar.TogeSimpleBackTopBar
import kotlinx.coroutines.CoroutineScope

@Composable
fun CallWaitingRouter(
    navBackStackEntry: NavBackStackEntry,
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    navController: NavController = rememberNavController(),
) {

    val viewModel: CallWaitingViewModel = hiltViewModel()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.errorEffect.collect { error ->
            if (error is TogeError.RoomNotFound) {
                viewModel.onEvent(CallWaitingEvent.SwitchWrongRoomCodeDialog)
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                is CallWaitingEffect.SuccessWaitingEnterRoom -> {
                    CallNavDestination.navigateToCallFromWaiting(navController , viewModel.uiState.value.roomCode )
                }

                is CallWaitingEffect.RejectedWaitingEnterRoom -> {
                    viewModel.onEvent(CallWaitingEvent.SwitchLeaveRoomDialog)
                }

                CallWaitingEffect.SuccessRequestEnterRoom -> {}
            }
        }
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()


    CallWaitingScreen(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        /* STATE */
        isLoading = state.isLoading,
        roomCode = state.roomCode,
        isShowDialogForWrongRoomCode = state.isShowDialogForWrongRoomCode,
        isShowDialogForLeaveRoom = state.isShowDialogForLeaveRoom,
        /* EVENT */
        onEventDisconnect = { viewModel.onEvent(CallWaitingEvent.Disconnect) },
        onEventSwitchLeaveRoomDialog = { viewModel.onEvent(CallWaitingEvent.SwitchLeaveRoomDialog) },
        onEventSwitchWrongRoomCodeDialog = { viewModel.onEvent(CallWaitingEvent.SwitchWrongRoomCodeDialog) },
        onBackClick = { navController.popBackStack() }
    )
}

@Composable
fun CallWaitingScreen(
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    /* STATE */
    isLoading: Boolean = false,
    roomCode: String = "",
    isShowDialogForWrongRoomCode: Boolean = false,
    isShowDialogForLeaveRoom: Boolean = false,
    /* EVENT */
    onEventDisconnect: () -> Unit = {},
    onEventSwitchLeaveRoomDialog: () -> Unit = {},
    onEventSwitchWrongRoomCodeDialog: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {

    val lottie by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.loading)
    )

    val scrollState = rememberScrollState()

    TogeDialog(
        isShowDialog = isShowDialogForLeaveRoom,
        title = stringResource(R.string.notify),
        content = stringResource(R.string.confirm_cancel_waiting),
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        onDismiss = { onEventSwitchLeaveRoomDialog() },
        onConfirm = {
            onEventDisconnect()
            onEventSwitchLeaveRoomDialog()
            onBackClick()
        }
    )

    TogeScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TogeSimpleBackTopBar(
                onClickBack = { onEventSwitchLeaveRoomDialog() }
            )
        },
        snackbarHost = { TogeSnackbarHost(hostState = snackbarHostState) },
        isLoading = isLoading,
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = lottie,
                iterations = LottieConstants.IterateForever,
                modifier = modifier.size(160.dp)
            )

            Spacer(modifier = modifier.size(24.dp))

            Text(
                text = stringResource(R.string.waiting_for_host_approval),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    TogeOnlyConfirmBtnDialog(
        isShowDialog = isShowDialogForWrongRoomCode,
        title = stringResource(R.string.error),
        content = stringResource(R.string.invalid_code),
        onConfirm = {
            onEventDisconnect()
            onEventSwitchWrongRoomCodeDialog()
            onBackClick()
        }
    )

    TogeOnlyConfirmBtnDialog(
        isShowDialog = isShowDialogForLeaveRoom,
        title = stringResource(R.string.reject),
        content = stringResource(R.string.host_rejected_entry),
        onConfirm = {
            onEventDisconnect()
            onEventSwitchLeaveRoomDialog()
            onBackClick()
        }
    )
}

@Preview
@Composable
fun PreviewCallWaitingScreen() {
    CallWaitingScreen(
        isLoading = false,
        roomCode = "123456",
        onEventDisconnect = {},
        onEventSwitchLeaveRoomDialog = {},
        onEventSwitchWrongRoomCodeDialog = {},
        onBackClick = {}
    )
}