package example.beechang.together.ui.component.topbar

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R
import example.beechang.together.ui.component.util.CircleRippleEffectForIcon


@Composable
fun TogeSimpleBackTopBar(
    modifier: Modifier = Modifier,
    onClickBack: () -> Unit,
    title: String = ""
) {
    TogeBaseTopBar(
        modifier = modifier,
        leftContent = {
            CircleRippleEffectForIcon(
                onClick = onClickBack
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Navigate up"
                )
            }
        },
        centerContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
        },
        weightTriple = Triple(0.5f, 1f, 0.5f)
    )
}

@Preview
@Composable
fun PreviewTogeSimpleBackTopBar() {
    MaterialTheme {
        TogeSimpleBackTopBar(
            onClickBack = { },
            title = "Title"
        )
    }
}