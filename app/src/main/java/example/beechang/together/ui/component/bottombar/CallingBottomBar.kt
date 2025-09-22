package example.beechang.together.ui.component.bottombar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.component.button.TogeCircleIconButton
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun CallingBottomBar(
    modifier: Modifier = Modifier,
    isCameraOn: Boolean = true,
    isMicOn: Boolean = true,
    useNavigationBarsPadding: Boolean = true,
    onClickCamera: () -> Unit = {},
    onClickMic: () -> Unit = {},
    onClickParticipant: () -> Unit = {},
    onClickChat: () -> Unit = {},
    onClickMore: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .then(if (useNavigationBarsPadding) Modifier.navigationBarsPadding() else Modifier),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LocalTogeAppColor.current.grey900)

        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TogeCircleIconButton(
                        icon = if (isCameraOn) {
                            R.drawable.ic_photo_camera
                        } else {
                            R.drawable.ic_no_photo_camera
                        },
                        onClick = {
                            onClickCamera()
                        },
                        width = 56.dp,
                        containerColor = if (isCameraOn) {
                            LocalTogeAppColor.current.grey700
                        } else {
                            LocalTogeAppColor.current.crimson100
                        },
                        contentColor = if (isCameraOn) {
                            LocalTogeAppColor.current.white
                        } else {
                            LocalTogeAppColor.current.crimson999
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    TogeCircleIconButton(
                        icon = if (isMicOn) {
                            R.drawable.ic_mic
                        } else {
                            R.drawable.ic_mic_off
                        },
                        onClick = { onClickMic() },
                        width = 56.dp,
                        containerColor = if (isMicOn) {
                            LocalTogeAppColor.current.grey700
                        } else {
                            LocalTogeAppColor.current.crimson100
                        },
                        contentColor = if (isMicOn) {
                            LocalTogeAppColor.current.white
                        } else {
                            LocalTogeAppColor.current.crimson999
                        }
                    )

                    Spacer(modifier = Modifier.width(4.dp))
                    TogeCircleIconButton(
                        icon = R.drawable.ic_group,
                        onClick = { onClickParticipant() },
                        width = 56.dp,
                        containerColor = LocalTogeAppColor.current.grey700,
                    )

//                    TODO: 출시 이후에 처리 미완료 기능 주석처리
//                    Spacer(modifier = Modifier.width(4.dp))
//                    TogeCircleIconButton(
//                        icon = R.drawable.ic_chat,
//                        onClick = { onClickChat() },
//                        width = 56.dp,
//                        containerColor = LocalTogeAppColor.current.grey700,
//                    )
                }

                Row(
                    modifier = Modifier.wrapContentWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp)
                            .background(LocalTogeAppColor.current.grey800.copy(alpha = 0.6f))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TogeCircleIconButton(
                        icon = R.drawable.ic_more,
                        onClick = { onClickMore() },
                        width = 32.dp,
                        containerColor = LocalTogeAppColor.current.grey700,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCallingBottomBarCameraOn() {
    CallingBottomBar(
        isCameraOn = true
    )
}

@Preview
@Composable
fun PreviewCallingBottomBarCameraOff() {
    CallingBottomBar(
        isCameraOn = false
    )
}

@Preview
@Composable
fun PreviewCallingBottomBarMicOn() {
    CallingBottomBar(
        isMicOn = true
    )
}

@Preview
@Composable
fun PreviewCallingBottomBarMicOff() {
    CallingBottomBar(
        isMicOn = false
    )
}

