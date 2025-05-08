package example.beechang.together.ui.component.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun TogeProfileItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String = "",
    showChangeButton: Boolean = false,
    onChangeClick: () -> Unit = {},
    description: String? = null,
    borderColor: Color = LocalTogeAppColor.current.grey900,
    borderWidth: Dp = 1.dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(width = borderWidth, color = borderColor)
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 16.dp,
                    end = if (showChangeButton) 0.dp else 16.dp,
                    bottom = 16.dp
                )
        ) {
            val (textColumn, buttonRef) = createRefs()

            Column(
                modifier = Modifier.constrainAs(textColumn) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(if (showChangeButton) buttonRef.start else parent.end, margin = 8.dp)
                    bottom.linkTo(parent.bottom)
                    width = Dimension.fillToConstraints
                }
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalTogeAppColor.current.grey300
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalTogeAppColor.current.grey600
                    )
                } else {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalTogeAppColor.current.white
                    )
                }
            }

            if (showChangeButton) {
                TextButton(
                    onClick = onChangeClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = LocalTogeAppColor.current.secondary500
                    ),
                    modifier = Modifier.constrainAs(buttonRef) {
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                    contentPadding = contentPadding(
                        start = 16.dp,
                        end = 16.dp,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.changing_profile),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Preview(name = "button on")
@Composable
fun PreviewUserMyPageProfileItem() {
    TogeProfileItem(
        label = "label",
        value = "value",
        showChangeButton = true,
        onChangeClick = {},
        description = "변경할 수 있습니다."
    )
}

@Preview(name = "button off")
@Composable
fun PreviewUserMyPageProfileItem2() {
    TogeProfileItem(
        label = "label",
        value = "value",
        showChangeButton = false,
        onChangeClick = {},
        description = "변경할 수 있습니다."
    )
}

@Preview(name = "discription not null / button off")
@Composable
fun PreviewUserMyPageProfileItem3() {
    TogeProfileItem(
        label = "label",
        value = "value",
        showChangeButton = false,
        onChangeClick = {},
        description = "decription",
    )
}

@Preview(name = "discription not null / button on")
@Composable
fun PreviewUserMyPageProfileItem4() {
    TogeProfileItem(
        label = "label",
        value = "value",
        showChangeButton = true,
        onChangeClick = {},
        description = "decription decription decription" +
                "decription decription decription" +
                "decription decription decription",
    )
}