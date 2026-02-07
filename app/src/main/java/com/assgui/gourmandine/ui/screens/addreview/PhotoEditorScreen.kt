package com.assgui.gourmandine.ui.screens.addreview

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.assgui.gourmandine.ui.theme.AppColors
import java.io.File
import java.io.FileOutputStream

@Composable
fun PhotoEditorScreen(
    sourceUri: Uri,
    onResult: (Uri) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val sourceBitmap = remember(sourceUri) {
        context.contentResolver.openInputStream(sourceUri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    }

    val sensor = remember { AccelerometerSensor(context) }
    val tiltX by sensor.tiltX.collectAsState()
    val tiltY by sensor.tiltY.collectAsState()

    DisposableEffect(Unit) {
        sensor.start()
        onDispose { sensor.stop() }
    }

    val canvasView = remember { PhotoEditorCanvasView(context) }

    // Set image once
    remember(sourceBitmap) {
        sourceBitmap?.let { canvasView.setImage(it) }
    }

    // Update filters from sensor
    canvasView.setFilterIntensity(sepia = tiltX, brightness = tiltY)

    val emojiOptions = remember {
        listOf("⭐", "❤️", "🔥", "👍", "😍", "🍽️", "👨‍🍳", "🎉")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Retouche photo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
            )
        }

        // Canvas view
        AndroidView(
            factory = { canvasView },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // Sensor indicators
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Sépia", color = Color.White, fontSize = 12.sp, modifier = Modifier.width(70.dp))
                LinearProgressIndicator(
                    progress = { tiltX },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AppColors.OrangeAccent,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    "${(tiltX * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp).padding(start = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Luminosité", color = Color.White, fontSize = 12.sp, modifier = Modifier.width(70.dp))
                LinearProgressIndicator(
                    progress = { tiltY },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AppColors.OrangeAccent,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    "${(tiltY * 100).toInt()}%",
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.width(40.dp).padding(start = 8.dp)
                )
            }
        }

        // Emoji sticker bar
        Text(
            text = "Stickers",
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            items(emojiOptions) { emoji ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { canvasView.addEmoji(emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emoji,
                        fontSize = 28.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Validate button
        Button(
            onClick = {
                val bitmap = canvasView.exportBitmap()
                val file = File(context.cacheDir, "edited_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, out)
                }
                onResult(Uri.fromFile(file))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent)
        ) {
            Text("Valider", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
