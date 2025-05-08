package example.beechang.together.ui.component.text

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun TogeTextFieldTheme(
    content: @Composable () -> Unit
) {
    val togeTextSelectionColors = TextSelectionColors(
        handleColor = MaterialTheme.colorScheme.secondary,
        backgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
    )

    CompositionLocalProvider(
        LocalTextSelectionColors provides togeTextSelectionColors
    ) {
        content()
    }
}