package example.beechang.together.ui.component.text


import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import kotlin.Boolean

@Composable
fun TogeBasicTextField(
    modifier: Modifier = Modifier,
    contentText: String,
    hintText: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    imeClickAction: () -> Unit = {},
    readOnly: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    focusRequester: FocusRequester = FocusRequester(),
    focusManager: FocusManager = LocalFocusManager.current,
) {
    TogeTextFieldTheme {
        BasicTextField(
            value = contentText,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = modifier
               .fillMaxWidth()
               .focusRequester(focusRequester),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    imeClickAction()
                    focusManager.clearFocus()
                },
                onGo = {
                    imeClickAction()
                    focusManager.clearFocus()
                },
                onNext = {
                    imeClickAction()
                    focusManager.moveFocus(FocusDirection.Next)
                },
                onPrevious = {
                    imeClickAction()
                    focusManager.moveFocus(FocusDirection.Previous)
                },
                onSearch = {
                    imeClickAction()
                    focusManager.clearFocus()
                },
                onSend = {
                    imeClickAction()
                    focusManager.clearFocus()
                }
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Box(modifier = Modifier) {
                    if (contentText.isEmpty()) {
                        Text(
                            text = hintText,
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            ),
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}