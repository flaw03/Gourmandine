package com.assgui.gourmandine.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.assgui.gourmandine.R.array.com_google_android_gms_fonts_certs
)

// Display font — elegant serif for titles & branding
private val playfairDisplay = GoogleFont("Playfair Display")
val DisplayFontFamily = FontFamily(
    Font(googleFont = playfairDisplay, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = playfairDisplay, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = playfairDisplay, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = playfairDisplay, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = playfairDisplay, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// Body font — clean geometric sans for UI text
private val dmSans = GoogleFont("DM Sans")
val BodyFontFamily = FontFamily(
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = dmSans, fontProvider = provider, weight = FontWeight.Bold),
)

object AppTypography {

    // ── Display / Hero ──────────────────────────────────────────────
    val heroTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.5).sp,
        color = AppColors.TextPrimary
    )

    val displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.3).sp,
        color = AppColors.TextPrimary
    )

    // ── Screen titles ───────────────────────────────────────────────
    val screenTitle = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        color = AppColors.TextPrimary
    )

    val sectionTitle = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = AppColors.TextPrimary
    )

    // ── Card content ────────────────────────────────────────────────
    val cardTitle = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.1).sp,
        color = AppColors.TextPrimary
    )

    val cardSubtitle = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = AppColors.TextSecondary
    )

    // ── Body text ───────────────────────────────────────────────────
    val bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = AppColors.TextPrimary
    )

    val bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = AppColors.TextSecondary
    )

    val bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = AppColors.TextTertiary
    )

    // ── Labels & Badges ─────────────────────────────────────────────
    val labelBold = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
        color = AppColors.OrangeAccent
    )

    val labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = AppColors.TextSecondary
    )

    val badge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = AppColors.OrangeAccent
    )

    // ── Buttons ─────────────────────────────────────────────────────
    val buttonLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = 0.2.sp
    )

    val buttonMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        letterSpacing = 0.2.sp
    )

    val buttonSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.3.sp
    )

    // ── Navigation ──────────────────────────────────────────────────
    val navLabel = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.2.sp
    )
}
