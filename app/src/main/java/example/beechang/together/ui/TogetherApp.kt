package example.beechang.together.ui

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
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

            // 시스템 window 부분 색상 설정
            val view = LocalView.current
            if (!LocalView.current.isInEditMode) {
                val window = (view.context as Activity).window
                SideEffect {
                    // status bar 배경을 compose 배경색과 동일하게
                    window.statusBarColor = DarkColorScheme.background.toArgb()

                    WindowInsetsControllerCompat(window, view).apply {
                        isAppearanceLightStatusBars = false //statusbar 흰색아이콘 고정
                        isAppearanceLightNavigationBars = false //navigationbar 흰색아이콘 고정
                    }
                    // 엑티비티에서도 한번 하긴함. 네비바 색상 모드색상 안따라가도록
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        window.isNavigationBarContrastEnforced = false
                    }
                }
            }

            content()
        }
    }
}
