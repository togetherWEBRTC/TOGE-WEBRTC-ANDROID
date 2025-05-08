package example.beechang.together.ui.component.text

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import example.beechang.together.R


@Composable
fun TogeOutLineTextField(
    value: String,
    onValueChange: (String) -> Unit,
    labelText: String = "",
    placeholderText: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: () -> Unit = {},
    leadingIcon: @Composable (() -> Unit)? = null,
    customTrailingIcon: @Composable (() -> Unit)? = null,
    passwordToggleIcons: Pair<Int, Int> = Pair(
        R.drawable.ic_visibility,
        R.drawable.ic_visibility_off
    ),
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    minLines: Int = 1,
    cornerRadius: Int = 12,
    textStyle: TextStyle = TextStyle.Default,
    focusRequester: FocusRequester = FocusRequester(),
    focusManager: FocusManager = LocalFocusManager.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.secondary),
    modifier: Modifier = Modifier,
) {
    TogeTextFieldTheme {
        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(labelText) },
            placeholder = { Text(placeholderText) },
            leadingIcon = leadingIcon,
            trailingIcon = if (isPassword && customTrailingIcon == null) {
                {
                    val imageVector =
                        if (passwordVisible) passwordToggleIcons.first else passwordToggleIcons.second
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = imageVector),
                            contentDescription = null
                        )
                    }
                }
            } else {
                customTrailingIcon
            },
            shape = RoundedCornerShape(cornerRadius.dp),
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onImeAction()
                    focusManager.clearFocus()
                },
                onGo = {
                    onImeAction()
                    focusManager.clearFocus()
                },
                onNext = {
                    onImeAction()
                    focusManager.moveFocus(FocusDirection.Next)
                },
                onSearch = {
                    onImeAction()
                    focusManager.clearFocus()
                },
                onSend = {
                    onImeAction()
                    focusManager.clearFocus()
                },
                onPrevious = {
                    onImeAction()
                    focusManager.moveFocus(FocusDirection.Previous)
                }
            ),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                cursorColor = (cursorBrush as? SolidColor)?.value
                    ?: MaterialTheme.colorScheme.secondary
            ),
            modifier = modifier.focusRequester(focusRequester),
            interactionSource = interactionSource
        )
    }
}

@Preview
@Composable
fun PreviewTogeOutLineTextField() {
    TogeOutLineTextField(
        value = "Hello",
        onValueChange = {},
        labelText = "Label",
        placeholderText = "Placeholder",
        isPassword = false,
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Done,
        onImeAction = {},
        leadingIcon = null,
        customTrailingIcon = null,
        passwordToggleIcons = Pair(
            R.drawable.ic_visibility,
            R.drawable.ic_visibility_off
        ),
        enabled = true,
        readOnly = false,
        singleLine = true,
        maxLines = 1,
        minLines = 1,
        cornerRadius = 12,
    )
}