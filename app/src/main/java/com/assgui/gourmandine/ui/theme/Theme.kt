package com.assgui.gourmandine.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GourmandineColorScheme = lightColorScheme(
    primary = AppColors.OrangeAccent,
    onPrimary = Color.White,
    primaryContainer = AppColors.OrangeLight,
    onPrimaryContainer = AppColors.OrangeDark,
    secondary = AppColors.Amber,
    onSecondary = Color.White,
    secondaryContainer = Color(0x1AD4A853),
    onSecondaryContainer = AppColors.Amber,
    tertiary = AppColors.Green,
    onTertiary = Color.White,
    background = AppColors.SurfaceWarm,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.SurfaceCard,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.BackgroundGray,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.MediumGray,
    outlineVariant = AppColors.Divider,
    error = AppColors.Red,
    onError = Color.White,
)

@Composable
fun GourmandineTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GourmandineColorScheme,
        typography = Typography,
        content = content
    )
}
