package com.assgui.gourmandine.ui.components

import androidx.compose.animation.core.Animatable
import com.assgui.gourmandine.ui.theme.AppColors
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val screenHeight = with(density) { screenHeightDp.toPx() }
    val topOffset = with(density) { (screenHeightDp * 0.1f).toPx() }
    val scope = rememberCoroutineScope()

    val offsetY = remember { Animatable(screenHeight) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            offsetY.animateTo(topOffset, tween(300))
        } else {
            offsetY.animateTo(screenHeight, tween(300))
        }
    }

    if (visible || offsetY.value < screenHeight) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset(0, (offsetY.value + dragOffset).roundToInt()) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(Color.White)
            ) {
                // Drag handle zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        if (dragOffset > screenHeight * 0.15f) {
                                            offsetY.animateTo(screenHeight, tween(300))
                                            onDismiss()
                                        } else {
                                            offsetY.animateTo(topOffset, tween(200))
                                        }
                                        dragOffset = 0f
                                    }
                                },
                                onDragCancel = {
                                    scope.launch {
                                        offsetY.animateTo(topOffset, tween(200))
                                        dragOffset = 0f
                                    }
                                },
                                onVerticalDrag = { _, amount ->
                                    val newOffset = dragOffset + amount
                                    dragOffset = newOffset.coerceAtLeast(0f)
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
                            .background(AppColors.LightGray)
                    )
                }

                content()
            }
        }
    }
}