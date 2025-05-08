package example.beechang.together.ui.component.util

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
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
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (borderWidth > 0.dp) {
                    Modifier.border(borderWidth, borderColor, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentScale = contentScale,
    )
}

@Preview(name = "기본 프로필 이미지 (24dp)")
@Composable
private fun PreviewCircularImage() {
    CircularImage(
        imageUrl = R.drawable.ic_launcher_background,
    )
}


@Preview(name = "테두리 있는 프로필 이미지")
@Composable
private fun PreviewCircularImageBordered() {
    MaterialTheme {
        CircularImage(
            imageUrl = R.drawable.ic_launcher_background,
            size = 36.dp,
            borderWidth = 1.dp,
            borderColor = Color.Red
        )
    }
}
