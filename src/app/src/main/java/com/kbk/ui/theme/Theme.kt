package com.kbk.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private const val PINK_PRIMARY_LIGHT = 0xFFFF6699
private const val PINK_SECONDARY_LIGHT = 0xFFD94075
private const val PINK_TERTIARY_LIGHT = 0xFF81517E

private const val PINK_PRIMARY_DARK = 0xFFA6385A
private const val PINK_SECONDARY_DARK = 0xFFe03d74
private const val PINK_TERTIARY_DARK = 0xFFF3B4EC

private const val ON_PINK_LIGHT_PRIMARY = 0xFFFFFFFF
private const val ON_PINK_LIGHT_SECONDARY = 0xFFFFFFFF
private const val ON_PINK_LIGHT_TERTIARY = 0xFF4D254E

private const val ON_PINK_DARK_PRIMARY = 0xFFFFFFFF
private const val ON_PINK_DARK_SECONDARY = 0xFFFFFFFF
private const val ON_PINK_DARK_TERTIARY = 0xFF4D254E

private const val LIGHT_BG = 0xFFFFFFFF
private const val DARK_BG = 0xFF000000
private const val ON_LIGHT = 0xFF000000
private const val ON_DARK = 0xFFFFFFFF

val PinkPrimaryLight = Color(PINK_PRIMARY_LIGHT)
val PinkSecondaryLight = Color(PINK_SECONDARY_LIGHT)
val PinkTertiaryLight = Color(PINK_TERTIARY_LIGHT)

val PinkPrimaryDark = Color(PINK_PRIMARY_DARK)
val PinkSecondaryDark = Color(PINK_SECONDARY_DARK)
val PinkTertiaryDark = Color(PINK_TERTIARY_DARK)

val OnPinkLightPrimary = Color(ON_PINK_LIGHT_PRIMARY)
val OnPinkLightSecondary = Color(ON_PINK_LIGHT_SECONDARY)
val OnPinkLightTertiary = Color(ON_PINK_LIGHT_TERTIARY)

val OnPinkDarkPrimary = Color(ON_PINK_DARK_PRIMARY)
val OnPinkDarkSecondary = Color(ON_PINK_DARK_SECONDARY)
val OnPinkDarkTertiary = Color(ON_PINK_DARK_TERTIARY)

val LightBackground = Color(LIGHT_BG)
val DarkBackground = Color(DARK_BG)
val OnLightColor = Color(ON_LIGHT)
val OnDarkColor = Color(ON_DARK)

private val DarkColorScheme = darkColorScheme(
    primary = PinkPrimaryDark,
    onPrimary = OnPinkDarkPrimary,
    secondary = PinkSecondaryDark,
    onSecondary = OnPinkDarkSecondary,
    tertiary = PinkTertiaryDark,
    onTertiary = OnPinkDarkTertiary,
    background = DarkBackground,
    onBackground = OnDarkColor,
    surface = DarkBackground,
    onSurface = OnDarkColor
)

private val LightColorScheme = lightColorScheme(
    primary = PinkPrimaryLight,
    onPrimary = OnPinkLightPrimary,
    secondary = PinkSecondaryLight,
    onSecondary = OnPinkLightSecondary,
    tertiary = PinkTertiaryLight,
    onTertiary = OnPinkLightTertiary,
    background = LightBackground,
    onBackground = OnLightColor,
    surface = LightBackground,
    onSurface = OnLightColor
)

@Composable
fun KeystrokeBiometricsKeyboardSDKTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
