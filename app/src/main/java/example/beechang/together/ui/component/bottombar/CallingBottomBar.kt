package example.beechang.together.ui.component.bottombar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
    onClickCamera: () -> Unit = {},
    onClickMic: () -> Unit = {},
    onClickParticipant: () -> Unit = {},
    onClickChat: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LocalTogeAppColor.current.grey900)

        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
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
                    width = 60.dp,
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

                Spacer(modifier = Modifier.width(8.dp))

                TogeCircleIconButton(
                    icon = if (isMicOn) {
                        R.drawable.ic_mic
                    } else {
                        R.drawable.ic_mic_off
                    },
                    onClick = { onClickMic() },
                    width = 60.dp,
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

                Spacer(modifier = Modifier.width(8.dp))

                TogeCircleIconButton(
                    icon = R.drawable.ic_group,
                    onClick = { onClickParticipant() },
                    width = 60.dp,
                    containerColor = LocalTogeAppColor.current.grey700,
                )

                Spacer(modifier = Modifier.width(8.dp))

                TogeCircleIconButton(
                    icon = R.drawable.ic_chat,
                    onClick = { onClickChat() },
                    width = 60.dp,
                    containerColor = LocalTogeAppColor.current.grey700,
                )
            }
        }
        // device navigation bar height
        val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)
        Spacer(
            modifier = Modifier
                .height(with(LocalDensity.current) { navigationBarHeight.toDp() })
                .fillMaxWidth()
        )
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

