package com.assgui.gourmandine.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.assgui.gourmandine.data.model.Review
import com.assgui.gourmandine.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ReviewCard(
    review: Review,
    compact: Boolean = false,
    expanded: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250))
            .background(AppColors.OrangeBackground, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        if (compact) {
            // Compact : rangée image + nom + texte tronqué
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
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
        } else if (expanded) {
            // Expanded : plein détail sans troncature
            Column {
                // Header utilisateur
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AppColors.OrangeAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.userName.take(1).uppercase(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = review.userName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "${review.userReviewCount} avis${if (review.userCreatedAt > 0L) " · Membre ${formatMemberSince(review.userCreatedAt)}" else ""}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = formatDate(review.createdAt),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Étoiles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) AppColors.OrangeAccent else AppColors.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = review.rating.toString(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Texte complet
                if (review.text.isNotBlank()) {
                    Text(
                        text = review.text,
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Photos plein format avec carrousel
                if (review.imageUrls.isNotEmpty()) {
                    val photoPagerState = rememberPagerState(pageCount = { review.imageUrls.size })

                    Box {
                        HorizontalPager(
                            state = photoPagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { page ->
                            AsyncImage(
                                model = review.imageUrls[page],
                                contentDescription = "Photo ${page + 1}",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (review.imageUrls.size > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "${photoPagerState.currentPage + 1}/${review.imageUrls.size}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                repeat(review.imageUrls.size) { index ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (index == photoPagerState.currentPage) 7.dp else 5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (index == photoPagerState.currentPage) Color.White
                                                else Color.White.copy(alpha = 0.5f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Full card standard (non compact, non expanded)
            Column {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    if (review.imageUrls.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            val pagerState = rememberPagerState(pageCount = { review.imageUrls.size })
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

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(timestamp))

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
