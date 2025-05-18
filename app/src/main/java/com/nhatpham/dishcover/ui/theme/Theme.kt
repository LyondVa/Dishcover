package com.nhatpham.dishcover.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryLightColor,
    onPrimaryContainer = Color.White,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    secondaryContainer = SecondaryLightColor,
    onSecondaryContainer = Color.White,
    tertiary = AccentYellow,
    onTertiary = Color.Black,
    background = BackgroundColor,
    onBackground = TextPrimaryColor,
    surface = SurfaceColor,
    onSurface = TextPrimaryColor,
    error = ErrorColor,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = Color.White,
    primaryContainer = PrimaryDarkColor,
    onPrimaryContainer = Color.White,
    secondary = SecondaryColor,
    onSecondary = Color.White,
    secondaryContainer = SecondaryDarkColor,
    onSecondaryContainer = Color.White,
    tertiary = AccentYellow,
    onTertiary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = ErrorColor,
    onError = Color.White
)

val DishcoverShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp)
)

// Custom shapes for recipe cards
val RecipeCardShape = RoundedCornerShape(16.dp)
val FeaturedRecipeCardShape = RoundedCornerShape(20.dp)

@Composable
fun DishcoverTheme(
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = DishcoverShapes,
        content = content
    )
}