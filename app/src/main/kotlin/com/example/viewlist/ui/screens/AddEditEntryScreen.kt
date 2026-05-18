package com.example.viewlist.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.viewlist.data.model.Entry
import com.example.viewlist.ui.components.StarRating
import com.example.viewlist.ui.theme.AccentCyan
import com.example.viewlist.ui.theme.AccentViolet
import com.example.viewlist.ui.theme.Background
import com.example.viewlist.ui.theme.BorderColor
import com.example.viewlist.ui.theme.CardBg
import com.example.viewlist.ui.theme.DeleteRed
import com.example.viewlist.ui.theme.StarAmber
import com.example.viewlist.ui.theme.Surface
import com.example.viewlist.ui.theme.TextPrimary
import com.example.viewlist.ui.theme.TextSecondary
import com.example.viewlist.viewmodel.MainViewModel

val CATEGORIES = listOf("Film", "Anime", "Cartoon", "Series", "Documentary", "Other")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    entryId: Long?,
    initialStatus: String,
    onBack: () -> Unit,
    vm: MainViewModel = viewModel(),
) {
    var title       by remember { mutableStateOf("") }
    var impression  by remember { mutableStateOf("") }
    var rating      by remember { mutableFloatStateOf(0f) }
    var imageUrl    by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf("Film") }
    var status      by remember { mutableStateOf(initialStatus) }
    var existingEntry by remember { mutableStateOf<Entry?>(null) }
    var showUrlDialog by remember { mutableStateOf(false) }
    var urlInput    by remember { mutableStateOf("") }

    val isEditing = entryId != null

    LaunchedEffect(entryId) {
        if (entryId != null) {
            vm.findById(entryId)?.let { e ->
                existingEntry = e
                title = e.title
                impression = e.impression
                rating = e.rating
                imageUrl = e.imageUrl
                category = e.category
                status = e.status
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { imageUrl = it.toString() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Edit Entry" else "New Entry",
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = {
                            existingEntry?.let { vm.delete(it) }
                            onBack()
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = DeleteRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface,
                    titleContentColor = TextPrimary,
                    navigationIconContentColor = TextPrimary,
                ),
            )
        },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Banner ────────────────────────────────────────────────
            BannerSection(
                imageUrl = imageUrl,
                onGallery = { galleryLauncher.launch("image/*") },
                onUrl = { showUrlDialog = true },
                onRemove = { imageUrl = "" },
            )

            // ── Title ─────────────────────────────────────────────────
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
            )

            // ── Category chips ────────────────────────────────────────
            Column {
                Text("Category", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CATEGORIES.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Background,
                            ),
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CATEGORIES.drop(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentCyan,
                                selectedLabelColor = Background,
                            ),
                        )
                    }
                }
            }

            // ── Status toggle ─────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = status == "viewed",
                    onClick = { status = "viewed" },
                    label = { Text("Watched") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentCyan,
                        selectedLabelColor = Background,
                    ),
                    modifier = Modifier.weight(1f),
                )
                FilterChip(
                    selected = status == "planned",
                    onClick = { status = "planned" },
                    label = { Text("Planned") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentViolet,
                        selectedLabelColor = Background,
                    ),
                    modifier = Modifier.weight(1f),
                )
            }

            // ── Star rating (watched only) ────────────────────────────
            if (status == "viewed") {
                Column {
                    Text("Rating", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    StarRating(
                        rating = rating,
                        onRatingChange = { rating = it },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (rating > 0f) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "%.1f / 5.0".format(rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarAmber,
                        )
                    }
                }
            }

            // ── Impression / Notes ────────────────────────────────────
            OutlinedTextField(
                value = impression,
                onValueChange = { impression = it },
                label = { Text(if (status == "viewed") "Impression" else "Notes") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = fieldColors(),
                maxLines = 10,
            )

            // ── Save ──────────────────────────────────────────────────
            Button(
                onClick = {
                    val entry = Entry(
                        id = existingEntry?.id ?: 0L,
                        title = title.trim(),
                        impression = impression.trim(),
                        rating = rating,
                        imageUrl = imageUrl,
                        category = category,
                        status = status,
                        createdAt = existingEntry?.createdAt ?: System.currentTimeMillis(),
                    )
                    vm.save(entry)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (isEditing) "Update" else "Save",
                    style = MaterialTheme.typography.titleMedium,
                    color = Background,
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── URL dialog ────────────────────────────────────────────────────
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            containerColor = Surface,
            title = { Text("Image URL", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Paste image URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    imageUrl = urlInput
                    urlInput = ""
                    showUrlDialog = false
                }) { Text("Apply", color = AccentCyan) }
            },
            dismissButton = {
                TextButton(onClick = {
                    urlInput = ""
                    showUrlDialog = false
                }) { Text("Cancel", color = TextSecondary) }
            },
        )
    }
}

@Composable
private fun BannerSection(
    imageUrl: String,
    onGallery: () -> Unit,
    onUrl: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardBg),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(alpha = 0.45f)),
                contentAlignment = Alignment.TopEnd,
            ) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove image", tint = TextPrimary)
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(48.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onGallery,
                        border = BorderStroke(1.dp, AccentCyan),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery")
                    }
                    OutlinedButton(
                        onClick = onUrl,
                        border = BorderStroke(1.dp, AccentViolet),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentViolet),
                    ) {
                        Icon(Icons.Filled.Link, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("URL")
                    }
                }
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentCyan,
    unfocusedBorderColor = BorderColor,
    focusedLabelColor = AccentCyan,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentCyan,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)
