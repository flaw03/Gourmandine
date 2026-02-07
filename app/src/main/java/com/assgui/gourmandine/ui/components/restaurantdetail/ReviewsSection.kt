package com.assgui.gourmandine.ui.components.restaurantdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.ui.components.ReviewCard
import com.assgui.gourmandine.ui.components.SwipeableSheet
import com.assgui.gourmandine.ui.theme.AppColors

@Composable
fun ReviewsSection(
    reviews: List<Review>,
    googleReviews: List<Review> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    // Auto-select Google tab if no community reviews
    LaunchedEffect(reviews.size, googleReviews.size) {
        if (reviews.isEmpty() && googleReviews.isNotEmpty()) {
            selectedTabIndex = 1
        }
    }

    val currentReviews = if (selectedTabIndex == 0) reviews else googleReviews

    Column(modifier = modifier) {
        // Tabs
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

        Spacer(modifier = Modifier.height(8.dp))

        // Carousel for the selected tab
        if (currentReviews.isNotEmpty()) {
            ReviewsCarousel(
                reviews = currentReviews,
                onReviewClick = { selectedReview = it }
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

    // Full sheet
    ReviewsFullSheet(
        visible = selectedReview != null,
        communityReviews = reviews,
        googleReviews = googleReviews,
        initialTabIndex = selectedTabIndex,
        onDismiss = { selectedReview = null }
    )
}

@Composable
private fun ReviewsCarousel(
    reviews: List<Review>,
    onReviewClick: (Review) -> Unit
) {
    if (reviews.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { reviews.size })

    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 20.dp),
        pageSpacing = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        ReviewCard(
            review = reviews[page],
            compact = true,
            modifier = Modifier.clickable { onReviewClick(reviews[page]) }
        )
    }

    // Page indicators
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
                        .size(6.dp)
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

@Composable
fun ReviewsFullSheet(
    visible: Boolean,
    communityReviews: List<Review>,
    googleReviews: List<Review>,
    initialTabIndex: Int = 0,
    onDismiss: () -> Unit
) {
    SwipeableSheet(
        visible = visible,
        onDismiss = onDismiss
    ) {
        var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
        val currentReviews = if (selectedTabIndex == 0) communityReviews else googleReviews

        Column(modifier = Modifier.fillMaxSize()) {
            // Tabs
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
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Text(
                            text = "Communauté (${communityReviews.size})",
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

            val scrollState = rememberScrollState()
            val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = navigationBarPadding.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentReviews.isNotEmpty()) {
                    currentReviews.forEach { review ->
                        ReviewCard(review = review)
                    }
                } else {
                    Text(
                        text = if (selectedTabIndex == 0) "Aucun avis de la communauté" else "Aucun avis Google",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
