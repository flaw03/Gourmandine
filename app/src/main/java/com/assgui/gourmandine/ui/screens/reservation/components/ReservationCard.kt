package com.assgui.gourmandine.ui.screens.reservation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.assgui.gourmandine.data.model.Reservation
import com.assgui.gourmandine.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReservationCard(
    reservation: Reservation,
    onDelete: () -> Unit,
    onAddToCalendar: () -> Unit,
    onChangeDate: () -> Unit,
    onAddReview: () -> Unit,
    onViewOnMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.clickable(onClick = onViewOnMap),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restaurant image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(reservation.restaurantImageUrl.ifBlank { null })
                    .crossfade(true)
                    .build(),
                contentDescription = reservation.restaurantName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.BackgroundGray)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reservation.restaurantName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    StatusChip(isPast = reservation.isPast)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = formatDate(reservation.dateMs),
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Text(
                    text = "${reservation.partySize} convive${if (reservation.partySize > 1) "s" else ""}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                if (reservation.notes.isNotBlank()) {
                    Text(
                        text = reservation.notes,
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!reservation.isPast) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onChangeDate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Modifier", fontSize = 12.sp, maxLines = 1)
                }
                OutlinedButton(
                    onClick = onAddToCalendar,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Calendrier", fontSize = 12.sp, maxLines = 1)
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text("Annuler", fontSize = 12.sp, maxLines = 1)
                }
            }
        } else {
            Button(
                onClick = onAddReview,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent)
            ) {
                Text("Donner mon avis", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatusChip(isPast: Boolean) {
    Box(
        modifier = Modifier
            .background(
                if (isPast) AppColors.LightGray else AppColors.Green.copy(alpha = 0.15f),
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = if (isPast) "Passée" else "À venir",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isPast) Color.Gray else AppColors.Green
        )
    }
}

private fun formatDate(dateMs: Long): String {
    return SimpleDateFormat("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRENCH)
        .format(Date(dateMs))
        .replaceFirstChar { it.uppercase() }
}
