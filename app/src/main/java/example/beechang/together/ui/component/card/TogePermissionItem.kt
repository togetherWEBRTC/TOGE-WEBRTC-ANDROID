package example.beechang.together.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun TogePermissionItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    isGranted: Boolean,
    onAllowClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 1.dp,
            color = LocalTogeAppColor.current.grey900
        )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = if (!isGranted) 0.dp else 16.dp,
                    bottom = 16.dp
                )
        ) {
            val (iconRef, textColumn, statusRef) = createRefs()

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isGranted)
                            LocalTogeAppColor.current.success900.copy(alpha = 0.3f)
                        else
                            LocalTogeAppColor.current.grey800.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
                    .constrainAs(iconRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = if (isGranted) LocalTogeAppColor.current.success500 else LocalTogeAppColor.current.grey300,
                    modifier = Modifier
                        .size(24.dp)
                )
            }

            Column(
                modifier = Modifier.constrainAs(textColumn) {
                    top.linkTo(parent.top)
                    start.linkTo(iconRef.end, margin = 12.dp)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(if (!isGranted) statusRef.start else parent.end, margin = 8.dp)
                    width = Dimension.fillToConstraints
                }
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = LocalTogeAppColor.current.white
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isGranted) stringResource(R.string.permissions_granted) else stringResource(
                        R.string.permission_required
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isGranted) LocalTogeAppColor.current.success500 else LocalTogeAppColor.current.grey300
                )
            }

            if (isGranted) {
                AsyncImage(
                    modifier = Modifier
                        .size(24.dp)
                        .constrainAs(statusRef) {
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                        },
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(R.drawable.ic_icon_success)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )
            } else {
                TextButton(
                    onClick = onAllowClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LocalTogeAppColor.current.primary500
                    ),
                    modifier = Modifier.constrainAs(statusRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.allow),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
