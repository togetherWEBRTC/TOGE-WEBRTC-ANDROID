package example.beechang.together.ui.component.util

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CircleRippleEffectForIcon(
    modifier: Modifier = Modifier,
    buttonSize: Int = 40,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {

    Box(
        modifier = modifier
           .size(buttonSize.dp)
           .clip(CircleShape)
           .clickable(
              onClick = onClick,
              indication = LocalIndication.current,
              interactionSource = remember { MutableInteractionSource() }
           ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}