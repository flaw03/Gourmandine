package com.assgui.gourmandine.ui.screens.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.ui.screens.home.RestaurantFilter
import com.assgui.gourmandine.ui.theme.AppColors
import kotlin.math.roundToInt

private val distanceValues = listOf(0.5f, 1f, 2f, 5f, 10f)
private val distanceLabels = listOf("500 m", "1 km", "2 km", "5 km", "10 km")
private val noFilterIndex = distanceValues.size - 1

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    activeFilters: Set<RestaurantFilter>,
    maxDistanceKm: Float?,
    onToggleFilter: (RestaurantFilter) -> Unit,
    onDistanceChange: (Float?) -> Unit,
    onClearFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val initialIndex = if (maxDistanceKm == null) noFilterIndex
    else distanceValues.indexOfFirst { it >= maxDistanceKm }.takeIf { it >= 0 } ?: noFilterIndex

    var sliderRawValue by remember(maxDistanceKm) { mutableFloatStateOf(initialIndex.toFloat()) }
    val sliderIndex = sliderRawValue.roundToInt()

    val totalActive = activeFilters.size + if (maxDistanceKm != null) 1 else 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFFFAFAFA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = 24.dp)
        ) {
            // ─── Header ───────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Filtres", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, color = Color.Black)
                    if (totalActive > 0) {
                        Box(
                            modifier = Modifier
                                .background(AppColors.OrangeAccent, CircleShape)
                                .padding(horizontal = 9.dp, vertical = 3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("$totalActive", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                if (totalActive > 0) {
                    TextButton(onClick = {
                        onClearFilters()
                        sliderRawValue = noFilterIndex.toFloat()
                    }) {
                        Text("Tout effacer", color = AppColors.OrangeAccent, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AppColors.MediumGray.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 20.dp))
            Spacer(Modifier.height(20.dp))

            // ─── Disponibilité ────────────────────────────────────
            FilterSection(
                icon = Icons.Default.Schedule,
                iconTint = Color(0xFF388E3C),
                title = "Disponibilité",
                bgColor = Color(0xFFE8F5E9)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PepChip(
                        label = "Ouvert maintenant",
                        selected = RestaurantFilter.OPEN_NOW in activeFilters,
                        onClick = { onToggleFilter(RestaurantFilter.OPEN_NOW) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Note ─────────────────────────────────────────────
            FilterSection(
                icon = Icons.Default.Star,
                iconTint = Color(0xFFF9A825),
                title = "Note minimale",
                bgColor = Color(0xFFFFF8E1)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        RestaurantFilter.RATING_3_PLUS  to 3,
                        RestaurantFilter.RATING_35_PLUS to 4,
                        RestaurantFilter.RATING_4_PLUS  to 4,
                        RestaurantFilter.RATING_45_PLUS to 5
                    ).forEach { (filter, starCount) ->
                        RatingChip(
                            starCount = starCount,
                            selected = filter in activeFilters,
                            onClick = { onToggleFilter(filter) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Prix ─────────────────────────────────────────────
            FilterSection(
                icon = Icons.Default.Payments,
                iconTint = Color(0xFF1565C0),
                title = "Gamme de prix",
                bgColor = Color(0xFFE3F2FD)
            ) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PepChip(
                        label = "€",
                        selected = RestaurantFilter.PRICE_1 in activeFilters,
                        onClick = { onToggleFilter(RestaurantFilter.PRICE_1) }
                    )
                    PepChip(
                        label = "€€",
                        selected = RestaurantFilter.PRICE_2 in activeFilters,
                        onClick = { onToggleFilter(RestaurantFilter.PRICE_2) }
                    )
                    PepChip(
                        label = "€€€",
                        selected = RestaurantFilter.PRICE_3 in activeFilters,
                        onClick = { onToggleFilter(RestaurantFilter.PRICE_3) }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ─── Distance (slider) ────────────────────────────────
            FilterSection(
                icon = Icons.Default.LocationOn,
                iconTint = Color(0xFF6A1B9A),
                title = "Distance maximale",
                bgColor = Color(0xFFF3E5F5)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = if (sliderIndex == noFilterIndex) "Pas de limite" else distanceLabels[sliderIndex],
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp,
                            color = if (sliderIndex < noFilterIndex) AppColors.OrangeAccent else Color.LightGray
                        )
                        if (sliderIndex < noFilterIndex) {
                            Text(
                                text = "de vous",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Slider(
                        value = sliderRawValue,
                        onValueChange = { sliderRawValue = it },
                        valueRange = 0f..(distanceValues.size - 1).toFloat(),
                        steps = distanceValues.size - 2,
                        onValueChangeFinished = {
                            val km = if (sliderIndex == noFilterIndex) null else distanceValues[sliderIndex]
                            onDistanceChange(km)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = AppColors.OrangeAccent,
                            activeTrackColor = AppColors.OrangeAccent,
                            inactiveTrackColor = AppColors.MediumGray,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("500 m", fontSize = 11.sp, color = Color.Gray)
                        Text("10 km+", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ─── Bouton Appliquer ─────────────────────────────────
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (totalActive == 0) "Fermer"
                    else "Voir les résultats · $totalActive filtre${if (totalActive > 1) "s" else ""}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun FilterSection(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    bgColor: Color,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        }
        Spacer(Modifier.height(14.dp))
        content()
    }
}

@Composable
private fun PepChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) AppColors.OrangeAccent else Color.White,
        animationSpec = tween(180), label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else AppColors.OrangeAccent,
        animationSpec = tween(180), label = "chipText"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, AppColors.OrangeAccent),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
            Text(
                text = label,
                color = textColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun RatingChip(starCount: Int, selected: Boolean, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) AppColors.OrangeAccent else Color.White,
        animationSpec = tween(180), label = "ratingBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else AppColors.OrangeAccent,
        animationSpec = tween(180), label = "ratingContent"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, AppColors.OrangeAccent),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(starCount) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
