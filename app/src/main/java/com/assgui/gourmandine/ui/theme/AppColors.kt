package com.assgui.gourmandine.ui.theme

import androidx.compose.ui.graphics.Color

object AppColors {
    // ── Brand ───────────────────────────────────────────────────────
    val OrangeAccent = Color(0xFFE8652B)        // Warm terracotta — richer than pure orange
    val OrangeBackground = Color(0xFFFFF3ED)
    val OrangeLight = Color(0x1AE8652B)         // 10% terracotta
    val OrangeMedium = Color(0x33E8652B)        // 20% terracotta
    val OrangeDark = Color(0xFFC14E1C)          // Deep terracotta for pressed states
    val Amber = Color(0xFFD4A853)               // Gold accent for premium elements

    // ── Status ──────────────────────────────────────────────────────
    val Green = Color(0xFF2E7D42)               // Deep forest green — more refined
    val GreenLight = Color(0x1A2E7D42)
    val Red = Color(0xFFD43B2F)                 // Warm red — less aggressive
    val RedLight = Color(0x1AD43B2F)

    // ── Neutrals ────────────────────────────────────────────────────
    val LightGray = Color(0xFFD0D0D0)
    val MediumGray = Color(0xFFE0E0E0)
    val BackgroundGray = Color(0xFFF2F0ED)      // Warm gray instead of cold
    val GoogleBlue = Color(0xFF4285F4)

    // ── Semantic text ───────────────────────────────────────────────
    val TextPrimary = Color(0xFF1A1714)         // Warm black — not pure black
    val TextSecondary = Color(0xFF6B6560)        // Warm medium gray
    val TextTertiary = Color(0xFFADA8A2)         // Warm light gray
    val TextOnDark = Color(0xFFFFF8F4)          // Cream white on dark backgrounds

    // ── Surfaces ────────────────────────────────────────────────────
    val SurfaceWarm = Color(0xFFFCFAF7)         // Cream / ivory background
    val SurfaceCard = Color(0xFFFFFFFF)
    val SurfaceSheet = Color(0xFFFAF8F5)
    val SurfaceElevated = Color(0xFFFFF9F5)     // Slightly warm elevated cards
    val Divider = Color(0xFFEDE9E4)             // Warm divider

    // ── Overlay / Scrim ─────────────────────────────────────────────
    val ScrimDark = Color(0x991A1714)
    val ScrimLight = Color(0x4D1A1714)

    // ── Gradient pairs ──────────────────────────────────────────────
    val GradientWarmStart = Color(0xFFE8652B)
    val GradientWarmEnd = Color(0xFFD4A853)
}
