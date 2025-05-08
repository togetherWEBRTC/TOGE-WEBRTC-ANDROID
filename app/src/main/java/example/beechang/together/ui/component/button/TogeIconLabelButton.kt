package example.beechang.together.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor


@Composable
fun TogeIconLabelButton(
    @DrawableRes iconRes: Int,
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: Color = LocalTogeAppColor.current.primary500,
    contentColor: Color = LocalTogeAppColor.current.white,
    disabledContainerColor: Color = LocalTogeAppColor.current.primary500.copy(alpha = 0.7f),
    disabledContentColor: Color = LocalTogeAppColor.current.white,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        ),
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    ) {

        AsyncImage(
            modifier = Modifier.size(24.dp),
            model = ImageRequest.Builder(LocalContext.current)
                .data(iconRes)
                .build(),
            contentDescription = null
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Preview
@Composable
fun IconLabelButtonPreview() {
    TogeIconLabelButton(
        R.drawable.ic_keyboard_external,
        "Add",
        onClick = { },
    )
}