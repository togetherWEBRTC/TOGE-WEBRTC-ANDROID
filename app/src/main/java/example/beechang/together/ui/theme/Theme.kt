package example.beechang.together.ui.theme


import androidx.compose.material3.darkColorScheme

val DarkColorScheme = darkColorScheme(
    primary = MyColor.primary500,
    onPrimary = MyColor.white,
    primaryContainer = MyColor.primary900,
    onPrimaryContainer = MyColor.primary100,

    secondary = MyColor.secondary500,
    onSecondary = MyColor.white,
    secondaryContainer = MyColor.secondary900,
    onSecondaryContainer = MyColor.secondary100,

    tertiary = MyColor.crimson500,
    onTertiary = MyColor.white,
    tertiaryContainer = MyColor.crimson900,
    onTertiaryContainer = MyColor.crimson100,

    error = MyColor.crimson500,
    onError = MyColor.white,
    errorContainer = MyColor.crimson900,
    onErrorContainer = MyColor.crimson100,

    background = MyColor.grey999,
    onBackground = MyColor.grey100,

    surface = MyColor.grey900,
    onSurface = MyColor.grey100,
    surfaceVariant = MyColor.grey800,
    onSurfaceVariant = MyColor.grey300,

    outline = MyColor.grey600,
    outlineVariant = MyColor.grey700,

    scrim = MyColor.black.copy(alpha = 0.7f)
)

