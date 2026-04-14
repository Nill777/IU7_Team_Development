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

private const val PRIMARY_DARK = 0xFF1b1751
private const val SECONDARY_DARK = 0xFF1c72bb
private const val TERTIARY_DARK = 0x9500F8FF
private const val DARK_BG = 0xFF0d1628
private const val ON_PRIMARY_DARK = 0xFFFFFFFF
private const val ON_SECONDARY_DARK = 0xFF04fbff
private const val ON_TERTIARY_DARK = 0xFFFFFFFF
private const val ON_DARK = 0xFFFFFFFF

val PrimaryDark = Color(PRIMARY_DARK)
val SecondaryDark = Color(SECONDARY_DARK)
val TertiaryDark = Color(TERTIARY_DARK)
val DarkBackground = Color(DARK_BG)
val OnDarkPrimary = Color(ON_PRIMARY_DARK)
val OnDarkSecondary = Color(ON_SECONDARY_DARK)
val OnDarkTertiary = Color(ON_TERTIARY_DARK)
val OnDarkColor = Color(ON_DARK)

private const val PRIMARY_LIGHT = 0xFFCDE2F5
private const val SECONDARY_LIGHT = 0xFF1c72bb
private const val TERTIARY_LIGHT = 0x9500F8FF
private const val LIGHT_BG = 0xFFFFFFFF
private const val ON_PRIMARY_LIGHT = 0xFF000000
private const val ON_SECONDARY_LIGHT = 0xFF04fbff
private const val ON_TERTIARY_LIGHT = 0xFF000000
private const val ON_LIGHT = 0xFF000000

val PrimaryLight = Color(PRIMARY_LIGHT)
val SecondaryLight = Color(SECONDARY_LIGHT)
val TertiaryLight = Color(TERTIARY_LIGHT)
val LightBackground = Color(LIGHT_BG)
val OnLightPrimary = Color(ON_PRIMARY_LIGHT)
val OnLightSecondary = Color(ON_SECONDARY_LIGHT)
val OnLightTertiary = Color(ON_TERTIARY_LIGHT)
val OnLightColor = Color(ON_LIGHT)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnDarkPrimary,
    secondary = SecondaryDark,
    onSecondary = OnDarkSecondary,
    tertiary = TertiaryDark,
    onTertiary = OnDarkTertiary,
    background = DarkBackground,
    onBackground = OnDarkColor,
    surface = DarkBackground,
    onSurface = OnDarkColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnLightPrimary,
    secondary = SecondaryLight,
    onSecondary = OnLightSecondary,
    tertiary = TertiaryLight,
    onTertiary = OnLightTertiary,
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
