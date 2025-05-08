package example.beechang.together.ui.component.topbar

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.component.util.CircleRippleEffectForIcon

@Composable
fun TogeCloseTopBar(
    modifier: Modifier = Modifier,
    onCloseClick: () -> Unit
) {
    TogeBaseTopBar(
        modifier = modifier,
        rightContent = {
            CircleRippleEffectForIcon(
                onClick = onCloseClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_close),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Close"
                )
            }
        },
        weightTriple = Triple(0.1f, 0.1f, 1f)
    )
}

@Preview
@Composable
fun PreviewTogeCloseTopBar() {
    MaterialTheme {
        TogeCloseTopBar(
            onCloseClick = { }
        )
    }
}