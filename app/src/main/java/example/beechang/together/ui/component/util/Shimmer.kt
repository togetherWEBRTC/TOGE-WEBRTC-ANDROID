package example.beechang.together.ui.component.util

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun Modifier.shimmer(
    radius: Dp = 12.dp,
    colorList: List<Color> = listOf(
        LocalTogeAppColor.current.grey700.copy(alpha = 0.3f),
        LocalTogeAppColor.current.grey500,
        LocalTogeAppColor.current.grey700.copy(alpha = 0.3f)
    ),
    ratio: Float = 0.4f,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1100,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val shimmerWidth = screenWidthDp * ratio


    clip(RoundedCornerShape(radius))
        .background(
            brush = Brush.linearGradient(
                colors = colorList,
                start = Offset(
                    x = translateAnim - shimmerWidth.value,
                    y = translateAnim - shimmerWidth.value
                ),
                end = Offset(x = translateAnim, y = translateAnim)
            )
        )
}