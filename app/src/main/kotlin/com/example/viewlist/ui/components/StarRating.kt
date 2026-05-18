package com.example.viewlist.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.viewlist.ui.theme.StarAmber
import com.example.viewlist.ui.theme.TextSecondary

@Composable
fun StarRating(
    rating: Float,
    onRatingChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    starSize: Dp = 40.dp,
    maxStars: Int = 5,
) {
    Row(modifier = modifier) {
        repeat(maxStars) { index ->
            val fill = (rating - index).coerceIn(0f, 1f)
            Box(
                modifier = Modifier.size(starSize),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when {
                        fill >= 1f   -> Icons.Filled.Star
                        fill >= 0.5f -> Icons.Filled.StarHalf
                        else         -> Icons.Filled.StarBorder
                    },
                    contentDescription = "Star ${index + 1}",
                    tint = if (fill > 0f) StarAmber else TextSecondary,
                    modifier = Modifier.size(starSize),
                )
                // Split each star into left (half) and right (full) tap zones
                Row(modifier = Modifier.matchParentSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onRatingChange(index + 0.5f) },
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onRatingChange((index + 1).toFloat()) },
                    )
                }
            }
        }
    }
}
