package example.beechang.together.ui.component.bottombar


import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import example.beechang.together.R
import example.beechang.together.ui.component.text.TogeBasicTextField
import example.beechang.together.ui.component.text.TogeClickableText


@Composable
fun HomeBottomBar(
    modifier: Modifier = Modifier,
    isLogin: Boolean,
    inputText: String,
    localFocusManager: FocusManager,
    onInputTextChange: (String) -> Unit = {},
    onMoveLogin: () -> Unit = {},
    onMoveToCall: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(12.dp),
        ) {
            if (isLogin) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    val focusRequester = FocusRequester()
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier
                            .weight(1f) // Add weight to take available space
                            .padding(vertical = 12.dp)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) {
                                focusRequester.requestFocus()
                            }
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_keyboard_external),
                            contentDescription = null,
                        )

                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "|")
                        Spacer(modifier = Modifier.width(12.dp))

                        TogeBasicTextField(
                            contentText = inputText,
                            onValueChange = { newText -> onInputTextChange(newText) },
                            hintText = stringResource(id = R.string.input_code),
                            imeClickAction = { onMoveToCall() },
                            focusRequester = focusRequester,
                            focusManager = localFocusManager,
                        )
                    }

                    TogeClickableText(
                        text = stringResource(id = R.string.enter),
                        color = MaterialTheme.colorScheme.primary,
                        enabled = inputText.isNotEmpty(),
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        onClick = {
                            localFocusManager.clearFocus()
                            onMoveToCall()
                        }
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            onMoveLogin()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AsyncImage(
                        modifier = Modifier.size(24.dp),
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(R.drawable.ic_icon_error)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = stringResource(id = R.string.need_login),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
        // device navigation bar height
        val navigationBarHeight = WindowInsets.navigationBars.getBottom(LocalDensity.current)
        Spacer(
            modifier = Modifier
                .height(with(LocalDensity.current) { navigationBarHeight.toDp() })
                .fillMaxWidth()
        )
    }
}
