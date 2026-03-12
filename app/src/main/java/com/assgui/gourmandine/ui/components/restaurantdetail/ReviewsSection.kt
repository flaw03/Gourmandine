package com.assgui.gourmandine.ui.components.restaurantdetail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.ui.components.ReviewCard
import com.assgui.gourmandine.ui.theme.AppColors

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    googleReviews: List<Review> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var starFilter by remember { mutableStateOf<Int?>(null) }
    var selectedReviewIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(selectedTabIndex) { starFilter = null }

    LaunchedEffect(reviews.size, googleReviews.size) {
        if (reviews.isEmpty() && googleReviews.isNotEmpty()) selectedTabIndex = 1
    }

    val allCurrentReviews = if (selectedTabIndex == 0) reviews else googleReviews
    val filteredReviews = if (starFilter != null)
        allCurrentReviews.filter { it.rating.toInt() == starFilter }
    else
        allCurrentReviews

    Column(modifier = modifier) {
        // Onglets
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = AppColors.OrangeAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = AppColors.OrangeAccent
                )
            },
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Communauté (${reviews.size})",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 0) AppColors.OrangeAccent else Color.Gray
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "Google (${googleReviews.size})",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 1) AppColors.OrangeAccent else Color.Gray
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filtres par étoiles
        if (allCurrentReviews.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StarFilterChip(
                    label = "Tous",
                    selected = starFilter == null,
                    count = allCurrentReviews.size,
                    onClick = { starFilter = null }
                )
                for (star in 5 downTo 1) {
                    val count = allCurrentReviews.count { it.rating.toInt() == star }
                    if (count > 0) {
                        StarFilterChip(
                            star = star,
                            selected = starFilter == star,
                            count = count,
                            onClick = { starFilter = if (starFilter == star) null else star }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        // Carrousel
        if (filteredReviews.isNotEmpty()) {
            ReviewsCarousel(
                reviews = filteredReviews,
                onReviewClick = { index -> selectedReviewIndex = index }
            )
        } else if (allCurrentReviews.isNotEmpty()) {
            Text(
                text = "Aucun avis avec ${starFilter}★",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        } else {
            Text(
                text = if (selectedTabIndex == 0) "Aucun avis de la communauté" else "Aucun avis Google",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }

    // Popover avec carrousel (fond grisé automatique par Dialog)
    if (selectedReviewIndex != null) {
        Dialog(
            onDismissRequest = { selectedReviewIndex = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val popoverPagerState = rememberPagerState(
                    initialPage = selectedReviewIndex!!,
                    pageCount = { filteredReviews.size }
                )

                // Carrousel des reviews — hauteur fixe, scroll interne
                HorizontalPager(
                    state = popoverPagerState,
                    pageSpacing = 12.dp,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .verticalScroll(rememberScrollState())
                            .padding(4.dp)
                    ) {
                        ReviewCard(review = filteredReviews[page], expanded = true)
                    }
                }

                // Indicateurs en bas
                if (filteredReviews.size > 1) {
                    Row(
                        modifier = Modifier.padding(top = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(filteredReviews.size.coerceAtMost(8)) { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == popoverPagerState.currentPage) 9.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (index == popoverPagerState.currentPage) Color.White
                                        else Color.White.copy(alpha = 0.45f)
                                    )
                            )
                        }
                        if (filteredReviews.size > 8) {
                            Text(
                                "${popoverPagerState.currentPage + 1}/${filteredReviews.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StarFilterChip(
    label: String? = null,
    star: Int? = null,
    selected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) AppColors.OrangeAccent else AppColors.OrangeAccent.copy(alpha = 0.10f),
        animationSpec = tween(180), label = "chipBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else AppColors.OrangeAccent,
        animationSpec = tween(180), label = "chipContent"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        if (star != null) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = "$star",
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = label ?: "",
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "($count)",
            color = contentColor.copy(alpha = 0.75f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ReviewsCarousel(
    reviews: List<Review>,
    onReviewClick: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { reviews.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        pageSpacing = 12.dp,
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        ReviewCard(
            review = reviews[page],
            compact = true,
            modifier = Modifier.clickable { onReviewClick(page) }
        )
    }

    if (reviews.size > 1) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(reviews.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (index == pagerState.currentPage) 8.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage) AppColors.OrangeAccent
                            else AppColors.LightGray
                        )
                )
            }
        }
    }
}
