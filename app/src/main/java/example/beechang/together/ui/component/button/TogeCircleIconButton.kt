package example.beechang.together.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun TogeCircleIconButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalTogeAppColor.current.grey900,
    contentColor: Color = LocalTogeAppColor.current.white,
    iconSize: Dp = 24.dp,
    height: Dp = 52.dp,
    width: Dp = 52.dp,
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier
            .height(height)
            .width(width),
        shape = RoundedCornerShape(percent = 50),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(icon),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview
@Composable
fun PreviewTogeCircleIconButton() {
    TogeCircleIconButton(
        icon = R.drawable.ic_mic,
        onClick = {}
    )
}

@Preview
@Composable
fun PreviewTogeCircleIconButtonForLongWidth() {
    TogeCircleIconButton(
        icon = R.drawable.ic_arrow_back,
        containerColor = LocalTogeAppColor.current.crimson400,
        width = 60.dp,
        onClick = {}
    )
}