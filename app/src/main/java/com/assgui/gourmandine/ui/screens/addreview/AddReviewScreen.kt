package com.assgui.gourmandine.ui.screens.addreview

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.assgui.gourmandine.data.model.Restaurant
import com.assgui.gourmandine.ui.components.SwipeableLeftSheet
import com.assgui.gourmandine.ui.theme.AppColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddReviewScreen(
    restaurant: Restaurant,
    onDismiss: () -> Unit,
    onReviewSubmitted: () -> Unit,
    viewModel: AddReviewViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showPhotoEditor by remember { mutableStateOf(false) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var editingIndex by remember { mutableIntStateOf(-1) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    // Gallery picker
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            editingIndex = -1
            pendingPhotoUri = it
            showPhotoEditor = true
        }
    }

    // Camera URI
    val cameraImageUri = remember(context) {
        val file = File(context.cacheDir, "camera_photo.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            editingIndex = -1
            pendingPhotoUri = cameraImageUri
            showPhotoEditor = true
        }
    }

    // Camera permission
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraLauncher.launch(cameraImageUri)
        }
    }

    LaunchedEffect(uiState.isSubmitted) {
        if (uiState.isSubmitted) {
            onReviewSubmitted()
        }
    }

    // Photo editor overlay
    if (showPhotoEditor && pendingPhotoUri != null) {
        PhotoEditorScreen(
            sourceUri = pendingPhotoUri!!,
            onResult = { editedUri ->
                if (editingIndex >= 0) {
                    viewModel.replaceImage(editingIndex, editedUri)
                } else {
                    viewModel.addImage(editedUri)
                }
                showPhotoEditor = false
                pendingPhotoUri = null
                editingIndex = -1
            },
            onDismiss = {
                showPhotoEditor = false
                pendingPhotoUri = null
                editingIndex = -1
            }
        )
        return
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.visitDate ?: System.currentTimeMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.onVisitDateChange(it) }
                    showDatePicker = false
                }) {
                    Text("OK", color = AppColors.OrangeAccent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    SwipeableLeftSheet(
        visible = true,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.BackgroundGray)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.Black
                    )
                }
            }

            // Title
            Text(
                text = "Ajouter un avis",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Restaurant recap
            RestaurantRecap(restaurant = restaurant)

            Spacer(modifier = Modifier.height(24.dp))

            // Visit date
            Text(
                text = "Date de visite",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.BackgroundGray)
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = AppColors.OrangeAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (uiState.visitDate != null) {
                        SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE)
                            .format(Date(uiState.visitDate!!))
                    } else {
                        "Sélectionner une date"
                    },
                    fontSize = 15.sp,
                    color = if (uiState.visitDate != null) Color.Black else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Star rating
            Text(
                text = "Votre note",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            StarRatingInput(
                rating = uiState.rating,
                onRatingChange = viewModel::onRatingChange
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Comment
            Text(
                text = "Votre commentaire",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = viewModel::onCommentChange,
                placeholder = { Text("Partagez votre expérience...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.OrangeAccent,
                    cursorColor = AppColors.OrangeAccent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Photos
            Text(
                text = "Photos (${uiState.selectedImageUris.size}/5)",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.selectedImageUris.forEachIndexed { index, uri ->
                    PhotoThumbnail(
                        uri = uri,
                        onRemove = { viewModel.removeImage(index) },
                        onEdit = {
                            editingIndex = index
                            pendingPhotoUri = uri
                            showPhotoEditor = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PhotoActionButton(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Galerie",
                    onClick = { galleryLauncher.launch("image/*") },
                    enabled = uiState.selectedImageUris.size < 5
                )
                PhotoActionButton(
                    icon = Icons.Default.AddAPhoto,
                    label = "Appareil",
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    enabled = uiState.selectedImageUris.size < 5
                )
            }

            // Error
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = uiState.errorMessage!!,
                    color = AppColors.Red,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Submit
            Button(
                onClick = { showPreview = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent),
                enabled = !uiState.isSubmitting
            ) {
                Text("Publier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Preview overlay
    if (showPreview) {
        ReviewPreviewSheet(
            restaurant = restaurant,
            uiState = uiState,
            onDismiss = { showPreview = false },
            onConfirm = {
                showPreview = false
                viewModel.submitReview(restaurant.id)
            }
        )
    }
}

@Composable
private fun RestaurantRecap(restaurant: Restaurant) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.BackgroundGray)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Restaurant image
        if (restaurant.imageUrls.isNotEmpty()) {
            AsyncImage(
                model = restaurant.imageUrls.first(),
                contentDescription = restaurant.name,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = restaurant.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                maxLines = 1
            )
            if (restaurant.address.isNotBlank()) {
                Text(
                    text = restaurant.address,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AppColors.OrangeAccent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "${restaurant.rating}",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
private fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(5) { index ->
            val starIndex = index + 1
            Icon(
                imageVector = if (starIndex <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Étoile $starIndex",
                tint = if (starIndex <= rating) AppColors.OrangeAccent else AppColors.MediumGray,
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onRatingChange(starIndex) }
            )
        }
    }
}

@Composable
private fun PhotoThumbnail(
    uri: Uri,
    onRemove: () -> Unit,
    onEdit: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onEdit)
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Photo sélectionnée",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(18.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Supprimer",
                tint = Color.White,
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun PhotoActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    val alpha = if (enabled) 1f else 0.4f

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(AppColors.OrangeAccent.copy(alpha = 0.15f * alpha))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = AppColors.OrangeAccent.copy(alpha = alpha),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            color = AppColors.OrangeAccent.copy(alpha = alpha),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewPreviewSheet(
    restaurant: Restaurant,
    uiState: AddReviewUiState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    SwipeableLeftSheet(
        visible = true,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppColors.BackgroundGray)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.Black
                    )
                }
            }

            Text(
                text = "Aperçu de votre avis",
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Restaurant recap
            RestaurantRecap(restaurant = restaurant)

            Spacer(modifier = Modifier.height(20.dp))

            // Visit date
            if (uiState.visitDate != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = AppColors.OrangeAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Visite le ${SimpleDateFormat("dd MMMM yyyy", Locale.FRANCE).format(Date(uiState.visitDate))}",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Note :",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                repeat(5) { index ->
                    Icon(
                        imageVector = if (index < uiState.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (index < uiState.rating) AppColors.OrangeAccent else AppColors.MediumGray,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Comment
            Text(
                text = "Commentaire",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uiState.comment.ifBlank { "(aucun commentaire)" },
                fontSize = 14.sp,
                color = Color.DarkGray,
                lineHeight = 20.sp
            )

            // Photos
            if (uiState.selectedImageUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Photos (${uiState.selectedImageUris.size})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.selectedImageUris.forEach { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Photo",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.OrangeAccent),
                enabled = !uiState.isSubmitting
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirmer et publier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.BackgroundGray,
                    contentColor = Color.Black
                )
            ) {
                Text("Modifier", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
