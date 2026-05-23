package com.example.viewlist.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.viewlist.data.model.EntryWithGenres
import com.example.viewlist.ui.theme.AccentEmerald
import com.example.viewlist.ui.theme.AccentIndigo
import com.example.viewlist.ui.theme.AccentPrimary
import com.example.viewlist.ui.theme.BorderColor
import com.example.viewlist.ui.theme.CardBg
import com.example.viewlist.ui.theme.CardElevated
import com.example.viewlist.ui.theme.StarAmber
import com.example.viewlist.ui.theme.Surface
import com.example.viewlist.ui.theme.TextMuted
import com.example.viewlist.ui.theme.TextPrimary
import com.example.viewlist.ui.theme.TextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun EntryCard(
    item: EntryWithGenres,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val entry = item.entry
    val statusColor = if (entry.status == "viewed") AccentEmerald else AccentIndigo

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, BorderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            // Left status stripe
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight()
                    .background(statusColor),
            )

            // Poster
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(130.dp),
            ) {
                if (entry.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = if (entry.imageUrl.startsWith("/")) File(entry.imageUrl) else entry.imageUrl,
                        contentDescription = entry.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CardElevated),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Movie,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(30.dp),
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MiniChip(text = entry.category, textColor = TextSecondary, bgColor = Surface)
                    MiniChip(
                        text = if (entry.status == "viewed") "Переглянуто" else "Заплановано",
                        textColor = statusColor,
                        bgColor = statusColor.copy(alpha = 0.12f),
                    )
                }

                if (item.genres.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        item.genres.forEach { genre ->
                            MiniChip(
                                text = genre.name,
                                textColor = AccentPrimary,
                                bgColor = AccentPrimary.copy(alpha = 0.10f),
                            )
                        }
                    }
                }

                if (entry.status == "viewed" && entry.rating > 0f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        repeat(5) { i ->
                            val fill = (entry.rating - i).coerceIn(0f, 1f)
                            Icon(
                                imageVector = when {
                                    fill >= 0.75f -> Icons.Filled.Star
                                    fill >= 0.25f -> Icons.AutoMirrored.Filled.StarHalf
                                    else          -> Icons.Filled.StarBorder
                                },
                                contentDescription = null,
                                tint = if (fill > 0f) StarAmber else TextMuted,
                                modifier = Modifier.size(13.dp),
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(entry.rating),
                            style = MaterialTheme.typography.labelSmall,
                            color = StarAmber,
                        )
                    }
                }

                if (entry.status == "viewed" && entry.watchedAt > 0L) {
                    val dateStr = remember(entry.watchedAt) {
                        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entry.watchedAt))
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                    )
                }

                if (entry.impression.isNotBlank()) {
                    Text(
                        text = entry.impression,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniChip(text: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}
