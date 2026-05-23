package com.example.viewlist.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.viewlist.ui.theme.StarAmber
import com.example.viewlist.ui.theme.TextSecondary
import kotlin.math.roundToInt

@Composable
fun StarRating(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp,
    maxStars: Int = 5,
) {
    val density = LocalDensity.current
    val starSizePx = with(density) { starSize.toPx() }
    val totalPx = starSizePx * maxStars

    fun ratingFromX(x: Float): Float {
        val clamped = x.coerceIn(0f, totalPx)
        return ((clamped / totalPx * maxStars) * 10).roundToInt() / 10f
    }

    Row(
        modifier = modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown()
                onRatingChange(ratingFromX(down.position.x))
                var evt = awaitPointerEvent()
                while (evt.changes.any { it.pressed }) {
                    val change = evt.changes.first { it.pressed }
                    change.consume()
                    onRatingChange(ratingFromX(change.position.x))
                    evt = awaitPointerEvent()
                }
            }
        },
    ) {
        repeat(maxStars) { index ->
            val fill = (rating - index).coerceIn(0f, 1f)
            Icon(
                imageVector = when {
                    fill >= 0.75f -> Icons.Filled.Star
                    fill >= 0.25f -> Icons.AutoMirrored.Filled.StarHalf
                    else          -> Icons.Filled.StarBorder
                },
                contentDescription = null,
                tint = if (fill > 0f) StarAmber else TextSecondary,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
