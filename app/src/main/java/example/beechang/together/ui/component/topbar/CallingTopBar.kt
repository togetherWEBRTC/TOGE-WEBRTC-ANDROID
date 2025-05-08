package example.beechang.together.ui.component.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.component.button.TogeCircleIconButton
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun CallingTopBar(
    modifier: Modifier = Modifier,
    isVolumeOn: Boolean,
    onClickCallEnd: () -> Unit,
    onClickToggleSpeaker: () -> Unit,
    onClickSwitchCamera: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            horizontalArrangement = Arrangement.Start,
        ) {
            TogeCircleIconButton(
                icon = R.drawable.ic_arrow_back,
                onClick = { onClickCallEnd() },
            )
            Spacer(modifier = Modifier.width(8.dp))
            TogeCircleIconButton(
                icon = if (isVolumeOn) {
                    R.drawable.ic_volume_up
                } else {
                    R.drawable.ic_volume_off
                },
                containerColor = if (isVolumeOn) {
                    LocalTogeAppColor.current.grey900
                } else {
                    LocalTogeAppColor.current.crimson100
                },
                contentColor = if (isVolumeOn) {
                    LocalTogeAppColor.current.white
                } else {
                    LocalTogeAppColor.current.crimson999
                },
                onClick = { onClickToggleSpeaker() },
            )
            Spacer(modifier = Modifier.width(8.dp))
            TogeCircleIconButton(
                icon = R.drawable.ic_flip_camera,
                onClick = { onClickSwitchCamera() },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.End,
        ) {
            TogeCircleIconButton(
                icon = R.drawable.ic_call_end,
                width = 60.dp,
                containerColor = LocalTogeAppColor.current.crimson400,
                onClick = { onClickCallEnd() },
            )
        }
    }
}