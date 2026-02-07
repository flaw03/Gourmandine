package com.assgui.gourmandine.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SwipeableLeftSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val screenWidth = with(density) { screenWidthDp.toPx() }
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(screenWidth) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(visible) {
        if (visible) {
            offsetX.animateTo(0f, tween(300))
        } else {
            offsetX.animateTo(screenWidth, tween(300))
        }
    }

    if (visible || offsetX.value < screenWidth) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .offset { IntOffset((offsetX.value + dragOffset).roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (dragOffset > screenWidth * 0.3f) {
                                    offsetX.animateTo(screenWidth, tween(300))
                                    onDismiss()
                                } else {
                                    offsetX.animateTo(0f, tween(200))
                                }
                                dragOffset = 0f
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                offsetX.animateTo(0f, tween(200))
                                dragOffset = 0f
                            }
                        },
                        onHorizontalDrag = { _, amount ->
                            val newOffset = dragOffset + amount
                            dragOffset = newOffset.coerceAtLeast(0f)
                        }
                    )
                }
                .background(Color.White)
        ) {
            content()
        }
    }
}
