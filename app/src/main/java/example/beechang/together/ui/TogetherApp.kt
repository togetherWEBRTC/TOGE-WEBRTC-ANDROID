package example.beechang.together.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import example.beechang.together.ui.theme.DarkColorScheme
import example.beechang.together.ui.theme.LocalTogeAppColor
import example.beechang.together.ui.theme.MyColor
import example.beechang.together.ui.theme.Typography


@Composable
fun TogetherApp(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalTogeAppColor provides MyColor,
    ) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            typography = Typography
        ) {
            content()
        }
    }
}
