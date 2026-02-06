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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier
) {
    var selectedReview by remember { mutableStateOf<Review?>(null) }

    Column(modifier = modifier) {
        // Title
        Text(
            text = "Avis (${reviews.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Carousel
        ReviewsCarousel(
            reviews = reviews,
            onReviewClick = { selectedReview = it }
        )
    }

    // Full sheet
    ReviewsFullSheet(
        visible = selectedReview != null,
        reviews = reviews,
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
    reviews: List<Review>,
    onDismiss: () -> Unit
) {
    SwipeableSheet(
        visible = visible,
        onDismiss = onDismiss
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Avis (${reviews.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

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
                reviews.forEach { review ->
                    ReviewCard(review = review)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}