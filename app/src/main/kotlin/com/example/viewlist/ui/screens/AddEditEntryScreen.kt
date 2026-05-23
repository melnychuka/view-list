@file:Suppress("UNUSED_VALUE")
package com.example.viewlist.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.example.viewlist.data.model.Genre
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.viewlist.data.model.Entry
import com.example.viewlist.ui.components.StarRating
import com.example.viewlist.ui.theme.AccentEmerald
import com.example.viewlist.ui.theme.AccentIndigo
import com.example.viewlist.ui.theme.AccentPrimary
import com.example.viewlist.ui.theme.Background
import com.example.viewlist.ui.theme.BorderColor
import com.example.viewlist.ui.theme.CardBg
import com.example.viewlist.ui.theme.DeleteRed
import com.example.viewlist.ui.theme.StarAmber
import com.example.viewlist.ui.theme.Surface
import com.example.viewlist.ui.theme.TextMuted
import com.example.viewlist.ui.theme.TextPrimary
import com.example.viewlist.ui.theme.TextSecondary
import com.example.viewlist.viewmodel.MainViewModel

val CATEGORIES = listOf("Фільм", "Аніме", "Мультфільм", "Серіал", "Інше")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditEntryScreen(
    entryId: Long?,
    initialStatus: String,
    onBack: () -> Unit,
    vm: MainViewModel = viewModel(),
) {
    var title         by remember { mutableStateOf("") }
    var impression    by remember { mutableStateOf("") }
    var rating        by remember { mutableFloatStateOf(0f) }
    var imageUrl      by remember { mutableStateOf("") }
    var category      by remember { mutableStateOf("Фільм") }
    var status        by remember { mutableStateOf(initialStatus) }
    var existingEntry by remember { mutableStateOf<Entry?>(null) }
    var watchedAt       by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var selectedGenres  by remember { mutableStateOf<List<String>>(emptyList()) }
    var genreInput      by remember { mutableStateOf("") }
    var genreSuggestions by remember { mutableStateOf<List<Genre>>(emptyList()) }
    var showUrlDialog    by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDatePicker   by remember { mutableStateOf(false) }
    var urlInput      by remember { mutableStateOf("") }

    val isEditing = entryId != null
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(genreInput) {
        if (genreInput.isBlank()) { genreSuggestions = emptyList(); return@LaunchedEffect }
        delay(150)
        genreSuggestions = vm.searchGenres(genreInput).filter { it.name !in selectedGenres }
    }

    LaunchedEffect(entryId) {
        if (entryId != null) {
            vm.findById(entryId)?.let { e ->
                existingEntry = e
                title      = e.title
                impression = e.impression
                rating     = e.rating
                imageUrl   = e.imageUrl
                category   = e.category
                status     = e.status
                watchedAt  = if (e.watchedAt > 0L) e.watchedAt else System.currentTimeMillis()
                selectedGenres = vm.getGenresForEntry(entryId).map { it.name }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { picked ->
            scope.launch { imageUrl = copyImageToInternalStorage(context, picked) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) "Редагувати" else "Новий запис",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = TextSecondary,
                        )
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Видалити", tint = DeleteRed)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Banner
            BannerSection(
                imageUrl = imageUrl,
                onGallery = { galleryLauncher.launch("image/*") },
                onUrl = { showUrlDialog = true },
                onRemove = { imageUrl = "" },
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Назва") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = fieldColors(),
            )

            // Category
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionLabel("Категорія")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CATEGORIES.forEach { cat ->
                        val selected = category == cat
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) AccentPrimary.copy(alpha = 0.18f) else Surface)
                                .clickable { category = cat }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) AccentPrimary else TextSecondary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            // Genres
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Жанри")

                if (selectedGenres.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        selectedGenres.forEach { name ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AccentPrimary.copy(alpha = 0.14f))
                                    .padding(start = 10.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccentPrimary,
                                )
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Видалити жанр",
                                    tint = AccentPrimary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable { selectedGenres = selectedGenres - name },
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = genreInput,
                    onValueChange = { genreInput = it },
                    label = { Text("Додати жанр") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                    singleLine = true,
                    trailingIcon = {
                        if (genreInput.isNotBlank()) {
                            IconButton(onClick = {
                                val trimmed = genreInput.trim()
                                if (trimmed.isNotBlank() && trimmed !in selectedGenres) {
                                    selectedGenres = selectedGenres + trimmed
                                }
                                genreInput = ""
                                genreSuggestions = emptyList()
                            }) {
                                Icon(Icons.Filled.Add, contentDescription = "Додати", tint = AccentPrimary)
                            }
                        }
                    },
                )

                if (genreSuggestions.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface),
                    ) {
                        genreSuggestions.forEachIndexed { index, genre ->
                            Text(
                                text = genre.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (genre.name !in selectedGenres) {
                                            selectedGenres = selectedGenres + genre.name
                                        }
                                        genreInput = ""
                                        genreSuggestions = emptyList()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                            if (index < genreSuggestions.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(BorderColor),
                                )
                            }
                        }
                    }
                }
            }

            // Status toggle
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionLabel("Статус")
                StatusToggle(status = status, onStatusChange = { status = it })
            }

            // Watch date (watched only)
            if (status == "viewed") {
                val dateLabel = remember(watchedAt) {
                    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(watchedAt))
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Коли переглянуто")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Surface)
                            .clickable { showDatePicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                        )
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            // Rating (watched only)
            if (status == "viewed") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionLabel("Оцінка")
                    StarRating(
                        rating = rating,
                        onRatingChange = { rating = it },
                    )
                    if (rating > 0f) {
                        Text(
                            "%.1f / 5.0".format(rating),
                            style = MaterialTheme.typography.bodyMedium,
                            color = StarAmber,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            // Impression / Notes
            OutlinedTextField(
                value = impression,
                onValueChange = { impression = it },
                label = { Text(if (status == "viewed") "Враження" else "Нотатки") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                colors = fieldColors(),
                maxLines = 10,
            )

            // Save button
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
                        watchedAt = if (status == "viewed") watchedAt else 0L,
                        createdAt = existingEntry?.createdAt ?: System.currentTimeMillis(),
                    )
                    vm.saveWithGenres(entry, selectedGenres) { onBack() }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPrimary,
                    disabledContainerColor = AccentPrimary.copy(alpha = 0.3f),
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (isEditing) "Оновити" else "Зберегти",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (title.isNotBlank()) Background else TextMuted,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = watchedAt)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { watchedAt = it }
                    showDatePicker = false
                }) { Text("OK", color = AccentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Скасувати", color = TextSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // URL dialog
    if (showUrlDialog) {
        AlertDialog(
            onDismissRequest = { showUrlDialog = false },
            containerColor = Surface,
            title = { Text("URL зображення", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Вставте URL") },
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
                }) { Text("Застосувати", color = AccentPrimary) }
            },
            dismissButton = {
                TextButton(onClick = {
                    urlInput = ""
                    showUrlDialog = false
                }) { Text("Скасувати", color = TextSecondary) }
            },
        )
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Surface,
            title = { Text("Видалити запис?", color = TextPrimary) },
            text = {
                Text(
                    "«${existingEntry?.title ?: "Цей запис"}» буде видалено назавжди.",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    existingEntry?.let { vm.delete(it) }
                    showDeleteDialog = false
                    onBack()
                }) { Text("Видалити", color = DeleteRed, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Скасувати", color = TextSecondary)
                }
            },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(AccentPrimary),
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
    }
}

@Composable
private fun StatusToggle(status: String, onStatusChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Surface)
            .padding(3.dp),
    ) {
        listOf("viewed" to "Переглянуто", "planned" to "Заплановано").forEach { (value, label) ->
            val selected = status == value
            val activeColor = if (value == "viewed") AccentEmerald else AccentIndigo
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) activeColor.copy(alpha = 0.18f) else Color.Transparent)
                    .clickable { onStatusChange(value) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) activeColor else TextSecondary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
        }
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
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = if (imageUrl.startsWith("/")) File(imageUrl) else imageUrl,
                contentDescription = "Обкладинка",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Background.copy(alpha = 0.4f)),
                contentAlignment = Alignment.TopEnd,
            ) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Close, contentDescription = "Видалити зображення", tint = TextPrimary)
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
                    tint = TextMuted,
                    modifier = Modifier.size(44.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onGallery,
                        border = BorderStroke(1.dp, AccentPrimary.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPrimary),
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Галерея")
                    }
                    OutlinedButton(
                        onClick = onUrl,
                        border = BorderStroke(1.dp, AccentIndigo.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentIndigo),
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

private suspend fun copyImageToInternalStorage(context: Context, uri: Uri): String =
    withContext(Dispatchers.IO) {
        val dir  = File(context.filesDir, "images").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        file.absolutePath
    }

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentPrimary,
    unfocusedBorderColor = BorderColor,
    focusedLabelColor = AccentPrimary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentPrimary,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
)
