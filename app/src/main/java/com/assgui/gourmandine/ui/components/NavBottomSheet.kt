package com.assgui.gourmandine.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes
import kotlinx.coroutines.launch

/**
 * Sheet overlay dans le même arbre de composition (pas de Popup).
 * Le MapHeaderOverlay reste fixe au-dessus.
 * Supporte le drag-to-dismiss via le drag handle.
 */
@Composable
fun NavBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    BackHandler(onBack = onDismiss)

    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    var sheetHeight by remember { mutableIntStateOf(1) }

    // Animation d'entrée (slide up)
    LaunchedEffect(Unit) {
        offsetY.snapTo(sheetHeight.toFloat())
        offsetY.animateTo(0f, tween(300))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 72.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.value.toInt().coerceAtLeast(0)) }
                .onGloballyPositioned { sheetHeight = it.size.height }
                .clip(AppShapes.Sheet)
                .background(AppColors.SurfaceSheet)
        ) {
            // Drag handle — zone draggable pour dismiss
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (offsetY.value > sheetHeight * 0.2f) {
                                        offsetY.animateTo(sheetHeight.toFloat(), tween(250))
                                        onDismiss()
                                    } else {
                                        offsetY.animateTo(0f, tween(200))
                                    }
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                scope.launch {
                                    val newVal = (offsetY.value + dragAmount).coerceAtLeast(0f)
                                    offsetY.snapTo(newVal)
                                }
                            }
                        )
                    }
                    .padding(top = 12.dp, bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AppColors.OrangeLight)
                )
            }

            content()
        }
    }
}
