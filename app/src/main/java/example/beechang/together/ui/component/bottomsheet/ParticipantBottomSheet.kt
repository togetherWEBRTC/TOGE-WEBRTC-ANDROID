package example.beechang.together.ui.component.bottomsheet


import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.call.room.RoomParticipantUi
import example.beechang.together.ui.component.util.CircularImage
import example.beechang.together.ui.component.util.ClickShrinkEffect
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun ParticipantBottomSheet(
    modifier: Modifier = Modifier,
    modalSheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    isShow: Boolean = false,
    roomCode: String = "",
    waitingParticipants: List<RoomParticipantUi> = listOf(),
    participants: List<RoomParticipantUi> = listOf(),
    /* EVENT */
    onDismissRequest: () -> Unit = {},
    onApproveWaiting: (String/*userId*/) -> Unit = {},
    onRejectWaiting: (String/*userId*/) -> Unit = {}
) {
    if (isShow) {

        val clipboardManager = LocalClipboardManager.current

        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier
                .fillMaxSize()
                .padding(top = 64.dp),
            sheetState = modalSheetState
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {

                // 코드 복사
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.entered_room_number, roomCode),
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.width(4.dp))

                        ClickShrinkEffect(
                            onClick = { clipboardManager.setText(AnnotatedString(roomCode)) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_copy),
                                contentDescription = "copy",
                                tint = LocalContentColor.current,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // 대기자
                if (waitingParticipants.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.waiting_participant_title),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    itemsIndexed(
                        items = waitingParticipants,
                        key = { _, participant -> participant.userId + "_waiting" }
                    ) { index, participant ->
                        RoomParticipantWaitingList(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = if (index == 0) 0.dp else 8.dp),
                            participant = participant,
                            onApproveWaiting = { onApproveWaiting(participant.userId) },
                            onRejectWaiting = { onRejectWaiting(participant.userId) }
                        )
                    }
                }

                // 참여자
                item {
                    Text(
                        text = stringResource(R.string.participant_title),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(
                            top = if (waitingParticipants.isNotEmpty()) 16.dp else 0.dp,
                            bottom = 8.dp
                        )
                    )
                }

                itemsIndexed(
                    items = participants,
                    key = { _, participant -> participant.userId + "_participant" }
                ) { index, participant ->
                    RoomParticipantList(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = if (index == 0) 0.dp else 8.dp),
                        participant = participant
                    )
                }
            }
        }
    }
}

@Composable
fun RoomParticipantWaitingList(
    modifier: Modifier = Modifier,
    participant: RoomParticipantUi,
    onApproveWaiting: () -> Unit = {},
    onRejectWaiting: () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularImage(
            imageUrl = participant.getProfileFullUrl(),
            size = 32.dp,
            borderWidth = 0.4.dp,
        )

        Text(
            text = participant.name,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Button(
            onClick = { onApproveWaiting() },
            modifier = Modifier
                .padding(end = 8.dp)
                .size(width = 48.dp, height = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                text = stringResource(id = R.string.approve),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Button(
            onClick = { onRejectWaiting() },
            modifier = Modifier
                .size(width = 48.dp, height = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LocalTogeAppColor.current.crimson700),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Text(
                text = stringResource(id = R.string.reject),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun RoomParticipantList(
    modifier: Modifier = Modifier,
    participant: RoomParticipantUi
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularImage(
            imageUrl = participant.getProfileFullUrl(),
            size = 32.dp,
            borderWidth = 0.4.dp,
        )

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f),

            ) {
            Text(
                text = participant.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (participant.isOwner) {
                Text(
                    text = stringResource(id = R.string.label_host),
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = LocalTogeAppColor.current.grey400
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewParticipantWaitingBottomSheet() {
    ParticipantBottomSheet(
        onDismissRequest = { },
        isShow = true,
        modalSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        ),
        waitingParticipants = listOf(
            RoomParticipantUi(
                userId = "0",
                name = "User Name",
            )
        ),
        participants = listOf(
            RoomParticipantUi(
                userId = "0",
                name = "User Name First",
                isOwner = true
            ),
            RoomParticipantUi(
                userId = "1",
                name = "User Name Second",
                isOwner = false
            )
        ),
    )
}