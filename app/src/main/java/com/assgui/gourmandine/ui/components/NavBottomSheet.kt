package com.assgui.gourmandine.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.assgui.gourmandine.ui.theme.AppColors
import com.assgui.gourmandine.ui.theme.AppShapes

/**
 * ModalBottomSheet avec un gap transparent en haut = statusBar + 72dp.
 * Le MapHeaderOverlay reste visible derrière le gap transparent.
 * Le fond coloré et le contenu commencent sous le header.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        // Transparent : le header derrière est visible dans la zone du gap
        containerColor = Color.Transparent,
        scrimColor = Color.Transparent,
        tonalElevation = 0.dp,
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        // Gap transparent = zone du header (statusBar + 72dp)
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(72.dp)
        )

        // Contenu visible avec fond et coins arrondis en haut
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.Sheet)
                .background(AppColors.SurfaceSheet)
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
