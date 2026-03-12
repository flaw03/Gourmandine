package com.assgui.gourmandine.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTextStyles {
    val screenTitle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
    val sectionTitle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
    val cardTitle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
    val bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = AppColors.TextSecondary, lineHeight = 20.sp)
    val labelSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = AppColors.TextTertiary)
    val labelBold = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AppColors.OrangeAccent)
}
