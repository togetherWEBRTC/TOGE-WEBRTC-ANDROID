package example.beechang.together.ui.component.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun CircularImage(
    imageUrl: Any,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    contentDescription: String? = null,
    borderWidth: Dp = 0.dp,
    borderColor: Color = LocalTogeAppColor.current.grey999,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (borderWidth > 0.dp) Modifier.background(borderColor) else Modifier
            )
            .then(
                if (borderWidth > 0.dp) Modifier.padding(borderWidth) else Modifier
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = contentScale,
        )
    }
}

@Preview(name = "테두리 있는 프로필")
@Composable
private fun PreviewCircularImageBorderedCorrected() {
    CircularImage(
        imageUrl = R.drawable.ic_launcher_background,
        size = 24.dp,
        borderWidth = 2.dp,
        borderColor = Color.Red
    )
}

@Preview(name = "테두리 없는 프로필")
@Composable
private fun PreviewCircularImageNoBorderCorrected() {
    CircularImage(
        imageUrl = R.drawable.ic_launcher_background,
        size = 24.dp
    )
}
