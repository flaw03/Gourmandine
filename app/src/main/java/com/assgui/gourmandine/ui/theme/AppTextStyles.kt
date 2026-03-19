package com.assgui.gourmandine.ui.theme

/**
 * Legacy bridge — delegates to AppTypography.
 * Kept to avoid breaking existing callers.
 */
object AppTextStyles {
    val screenTitle get() = AppTypography.screenTitle
    val sectionTitle get() = AppTypography.sectionTitle
    val cardTitle get() = AppTypography.cardTitle
    val bodyMedium get() = AppTypography.bodyMedium
    val labelSmall get() = AppTypography.bodySmall
    val labelBold get() = AppTypography.labelBold
}
