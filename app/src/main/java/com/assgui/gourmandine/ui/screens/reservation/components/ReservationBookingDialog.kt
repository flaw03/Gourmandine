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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val TIME_SLOTS = buildList {
    for (hour in 12..21) {
        add(hour * 60)
        add(hour * 60 + 30)
    }
    add(22 * 60)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationBookingDialog(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onConfirm: (dateMs: Long, partySize: Int, notes: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var partySize by remember { mutableIntStateOf(2) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
    var selectedDateMs by remember { mutableLongStateOf(tomorrow.timeInMillis) }
    var selectedTimeMinutes by remember { mutableIntStateOf(12 * 60) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMs
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMs = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Réserver chez ${restaurant.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Date picker trigger
            Text("Date", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.BackgroundGray)
                    .clickable { showDatePicker = true }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AppColors.OrangeAccent
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
                        .format(selectedDateMs),
                    fontSize = 15.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time slots
            Text("Heure", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(TIME_SLOTS) { minutes ->
                    val isSelected = minutes == selectedTimeMinutes
                    val h = minutes / 60
                    val m = minutes % 60
                    val label = String.format("%02d:%02d", h, m)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AppColors.OrangeAccent else AppColors.BackgroundGray)
                            .clickable { selectedTimeMinutes = minutes }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color.Black,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Party size
            Text("Convives", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { if (partySize > 1) partySize-- },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.BackgroundGray)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Moins", tint = Color.Black)
                }
                Text(
                    text = "$partySize",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                IconButton(
                    onClick = { if (partySize < 10) partySize++ },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.BackgroundGray)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes
            Text("Notes (optionnel)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Allergie, occasion spéciale...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    val cal = Calendar.getInstance().apply {
                        timeInMillis = selectedDateMs
                        set(Calendar.HOUR_OF_DAY, selectedTimeMinutes / 60)
                        set(Calendar.MINUTE, selectedTimeMinutes % 60)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    onConfirm(cal.timeInMillis, partySize, notes)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent)
            ) {
                Text("Confirmer la réservation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
