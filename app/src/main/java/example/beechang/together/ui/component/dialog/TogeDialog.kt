package example.beechang.together.ui.component.dialog


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import example.beechang.together.R
import example.beechang.together.ui.theme.LocalTogeAppColor

@Composable
fun TogeDialog(
    modifier: Modifier = Modifier,
    title: String = "",
    content: String = "",
    confirmButtonText: String = stringResource(R.string.ok),
    dismissButtonText: String = stringResource(R.string.cancel),
    onDismiss: () -> Unit = { },
    onConfirm: () -> Unit = {},
    isShowDismissButton: Boolean = true,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = false,
    isShowDialog: Boolean = false,
) {
    if (isShowDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside
            )
        ) {
            Box(
                modifier = modifier
                    .widthIn(min = 280.dp, max = 560.dp)
                    .background(
                        color = LocalTogeAppColor.current.grey999,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .border(
                        width = 1.dp,
                        color = LocalTogeAppColor.current.grey800,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(modifier = Modifier.wrapContentSize()) {

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = LocalTogeAppColor.current.white,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalTogeAppColor.current.white,
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isShowDismissButton) {
                            Button(
                                modifier = Modifier,
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = LocalTogeAppColor.current.white),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = dismissButtonText,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = LocalTogeAppColor.current.black,
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Button(
                            modifier = Modifier,
                            onClick = onConfirm,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LocalTogeAppColor.current.primary500),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = confirmButtonText,
                                style = MaterialTheme.typography.titleSmall,
                                color = LocalTogeAppColor.current.white,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewTogeDialog() {
    TogeDialog(
        title = "TITLE",
        content = "CONTENT",
        confirmButtonText = "OK",
        dismissButtonText = "CLOSE",
        isShowDismissButton = true,
        isShowDialog = true
    )
}