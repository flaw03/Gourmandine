package com.assgui.gourmandine.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.assgui.gourmandine.ui.screens.home.RestaurantFilter
import com.assgui.gourmandine.ui.theme.AppColors

@Composable
fun FilterChipsRow(
    activeFilters: Set<RestaurantFilter>,
    onToggleFilter: (RestaurantFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(RestaurantFilter.entries) { filter ->
            val selected = filter in activeFilters
            FilterChip(
                selected = selected,
                onClick = { onToggleFilter(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.OrangeAccent,
                    selectedLabelColor = Color.White,
                    containerColor = AppColors.BackgroundGray,
                    labelColor = Color.Gray
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) AppColors.OrangeAccent else AppColors.MediumGray
                )
            )
        }
    }
}
