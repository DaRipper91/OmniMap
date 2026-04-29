package com.omnimap.presentation.graph.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.omnimap.domain.model.Node
import kotlin.math.roundToInt

@Composable
fun NodeComposable(
    node: Node,
    isSelected: Boolean,
    onNodeDragged: (String, Float, Float) -> Unit,
    onNodeSelected: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val alpha = if (node.isDraft) 0.5f else 1f

    Card(
        modifier = Modifier
            .offset { IntOffset(node.x.roundToInt(), node.y.roundToInt()) }
            .pointerInput(node.id) {
                detectTapGestures(
                    onTap = { onNodeSelected(node.id) }
                )
            }
            .pointerInput(node.id) {
                detectDragGestures(
                    onDragStart = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onNodeDragged(node.id, node.x + dragAmount.x, node.y + dragAmount.y)
                    }
                )
            }
            .widthIn(min = 140.dp, max = 280.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = node.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (node.description.isNotBlank()) {
                Text(
                    text = node.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}