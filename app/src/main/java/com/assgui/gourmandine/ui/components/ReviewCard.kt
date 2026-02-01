package com.assgui.gourmandine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.assgui.gourmandine.data.model.Review
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val OrangeAccent = Color(0xFFFF6B35)

@Composable
fun ReviewCard(
    review: Review,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF3ED), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (compact) {
            // Compact: single row — image + name + truncated text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Small image
                if (review.imageUrls.isNotEmpty()) {
                    AsyncImage(
                        model = review.imageUrls.first(),
                        contentDescription = "Photo avis",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = review.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = formatDate(review.createdAt),
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = review.text,
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )
                }
            }
        } else {
            // Full card
            Column {
                // Header: name + review count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = review.userName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${review.userReviewCount} avis",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Member since + review date
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Membre ${formatMemberSince(review.userCreatedAt)}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = formatDate(review.createdAt),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Image carousel + text side by side
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    // Image carousel
                    if (review.imageUrls.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            val pagerState =
                                rememberPagerState(pageCount = { review.imageUrls.size })
                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                AsyncImage(
                                    model = review.imageUrls[page],
                                    contentDescription = "Photo avis",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (review.imageUrls.size > 1) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    repeat(review.imageUrls.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (index == pagerState.currentPage) Color.White
                                                    else Color.White.copy(alpha = 0.4f)
                                                )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    // Review text
                    Text(
                        text = review.text,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        lineHeight = 18.sp,
                        maxLines = 6,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReviewItemPreview() {
    ReviewCard(
        review = Review(
            id = "1",
            restaurantId = "rest_123",
            userId = "user_42",
            userName = "Ahmed Benali",
            userReviewCount = 27,
            userCreatedAt = 1625140800000,
            imageUrls = listOf(
                "https://picsum.photos/200",
                "https://picsum.photos/201"
            ),
            text = "Très bon restaurant, service rapide et plats délicieux. Je recommande \uD83D\uDC4D",
            rating = 4.5,
            createdAt = System.currentTimeMillis()
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ReviewItemCompactPreview() {
    ReviewCard(
        review = Review(
            id = "1",
            restaurantId = "rest_123",
            userId = "user_42",
            userName = "Ahmed Benali",
            userReviewCount = 27,
            userCreatedAt = 1625140800000,
            imageUrls = listOf("https://picsum.photos/200"),
            text = "Très bon restaurant, service rapide et plats délicieux. Je recommande \uD83D\uDC4D",
            rating = 4.5,
            createdAt = System.currentTimeMillis()
        ),
        compact = true
    )
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
    return sdf.format(Date(timestamp))
}

private fun formatMemberSince(createdAt: Long): String {
    if (createdAt == 0L) return ""
    val diff = System.currentTimeMillis() - createdAt
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        days < 30 -> "depuis ${days}j"
        days < 365 -> "depuis ${days / 30} mois"
        else -> "depuis ${days / 365} an${if (days / 365 > 1) "s" else ""}"
    }
}
