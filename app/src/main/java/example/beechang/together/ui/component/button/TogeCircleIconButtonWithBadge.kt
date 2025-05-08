package example.beechang.together.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun TogeCircleIconButtonWithBadge(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = LocalTogeAppColor.current.grey900,
    contentColor: Color = LocalTogeAppColor.current.white,
    iconSize: Dp = 24.dp,
    height: Dp = 52.dp,
    width: Dp = 52.dp,
    showBadge: Boolean = false,
    badgeIcon: Int? = null,
    badgeColor: Color = LocalTogeAppColor.current.crimson999,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
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
}

