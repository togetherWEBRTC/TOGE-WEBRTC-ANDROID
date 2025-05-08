package example.beechang.together.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R


@Composable
fun TogeFloatingButtonWithIcon(
    enabled: Boolean,
    @DrawableRes iconRes: Int,
    text: String,
    onEnableClick: () -> Unit,
    onDisableClick: () -> Unit = {},
) {
    TogeIconLabelButton(
        iconRes = iconRes,
        text = text,
        enabled = enabled,
        contentPadding = PaddingValues(16.dp),
        onClick = {
            if (enabled) {
                onEnableClick()
            } else {
                onDisableClick()
            }
        }
    )
}


@Preview(
    showBackground = true,
    name = "IconLabelButton - enabled",
)
@Composable
fun PreviewTogeFloatingButtonWithIconEnabled() {
    TogeFloatingButtonWithIcon(
        enabled = true,
        iconRes = R.drawable.ic_video_call,
        text = stringResource(R.string.active_call),
        onEnableClick = {}
    )
}

@Preview(
    showBackground = true,
    name = "IconLabelButton - enabled",
)
@Composable
fun PreviewTogeFloatingButtonWithIconDisabled() {
    TogeFloatingButtonWithIcon(
        enabled = false,
        iconRes = R.drawable.ic_video_call,
        text = stringResource(R.string.active_call),
        onEnableClick = {}
    )
}