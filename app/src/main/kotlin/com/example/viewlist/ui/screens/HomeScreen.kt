@file:Suppress("UNUSED_VALUE")
package com.example.viewlist.ui.screens

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.viewlist.R
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewlist.data.model.EntryWithGenres
import com.example.viewlist.ui.components.EntryCard
import com.example.viewlist.ui.theme.AccentEmerald
import com.example.viewlist.ui.theme.PhilosopherFamily
import com.example.viewlist.ui.theme.AccentIndigo
import com.example.viewlist.ui.theme.AccentPrimary
import com.example.viewlist.ui.theme.Background
import com.example.viewlist.ui.theme.StarAmber
import com.example.viewlist.ui.theme.StarSilver
import com.example.viewlist.ui.theme.Surface
import com.example.viewlist.ui.theme.DeleteRed
import com.example.viewlist.ui.theme.TextMuted
import com.example.viewlist.ui.theme.TextPrimary
import com.example.viewlist.ui.theme.TextSecondary
import com.example.viewlist.viewmodel.MainViewModel
import com.example.viewlist.viewmodel.UpdateState
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

// Gradient colours used only in the header
private val HeaderTop    = Color(0xFF1C0F32)
private val HeaderBottom = Color(0xFF0B0F24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddEntry: (initialStatus: String) -> Unit,
    onViewEntry: (id: Long) -> Unit,
    onEditEntry: (id: Long) -> Unit,
    vm: MainViewModel = viewModel(),
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val viewed  by vm.viewed.collectAsStateWithLifecycle()
    val planned by vm.planned.collectAsStateWithLifecycle()
    val entries = if (selectedTab == 0) viewed else planned

    val context           = LocalContext.current
    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded      by remember { mutableStateOf(false) }
    var expandedItemId    by remember { mutableStateOf<Long?>(null) }
    var deleteTarget      by remember { mutableStateOf<EntryWithGenres?>(null) }
    val updateState       by vm.updateState.collectAsStateWithLifecycle()

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val json = context.contentResolver.openInputStream(uri)
                    ?.bufferedReader()?.readText() ?: return@launch
                vm.importJson(
                    json    = json,
                    onResult = { imp, skip ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Імпортовано: $imp, пропущено: $skip")
                        }
                    },
                    onError  = { msg ->
                        scope.launch { snackbarHostState.showSnackbar("Помилка: $msg") }
                    },
                )
            } catch (_: Exception) {
                snackbarHostState.showSnackbar("Не вдалося прочитати файл")
            }
        }
    }

    Scaffold(
        snackbarHost        = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick        = { onAddEntry(if (selectedTab == 0) "viewed" else "planned") },
                containerColor = AccentPrimary,
                contentColor   = Background,
                icon           = { Icon(Icons.Filled.Add, contentDescription = null) },
                text           = {
                    Text(
                        if (selectedTab == 0) "Додати перегляд" else "До планів",
                        fontWeight = FontWeight.SemiBold,
                    )
                },
            )
        },
        containerColor = Background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Sailor Moon — lower-left corner decoration
            Image(
                painter            = painterResource(R.drawable.bg_corner_sailormoon),
                contentDescription = null,
                contentScale       = ContentScale.Fit,
                alpha              = 0.13f,
                modifier           = Modifier
                    .height(220.dp)
                    .align(Alignment.BottomStart),
            )
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {

            // ══════════════════════════════════════════════
            //  Magical header
            // ══════════════════════════════════════════════
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(HeaderTop, HeaderBottom))
                    ),
            ) {
                // ── Star field ───────────────────────────
                Canvas(modifier = Modifier.matchParentSize()) {
                    listOf(
                        Triple(0.60f, 0.18f, 9f),
                        Triple(0.90f, 0.15f, 7f),
                        Triple(0.75f, 0.70f, 6f),
                        Triple(0.55f, 0.78f, 4f),
                        Triple(0.68f, 0.42f, 5f),
                        Triple(0.83f, 0.50f, 4f),
                        Triple(0.58f, 0.52f, 3f),
                        Triple(0.88f, 0.82f, 5f),
                    ).forEach { (xr, yr, r) ->
                        drawMagicStar(
                            center      = Offset(size.width * xr, size.height * yr),
                            outerRadius = r,
                            color       = if (r >= 7f) StarAmber else StarSilver,
                            alpha       = if (r >= 7f) 0.88f else 0.52f,
                        )
                    }
                }

                // ── Title row ────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 4.dp, top = 20.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text  = "Мій списочок",
                            style = MaterialTheme.typography.headlineLarge,
                            color = AccentPrimary,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text  = "Твій особистий медіащоденник",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Меню",
                                tint = StarSilver,
                            )
                        }
                        DropdownMenu(
                            expanded          = menuExpanded,
                            onDismissRequest  = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text    = { Text("Експорт даних") },
                                onClick = {
                                    menuExpanded = false
                                    vm.exportJson { json -> shareBackupFile(context, json) }
                                },
                            )
                            DropdownMenuItem(
                                text    = { Text("Імпорт даних") },
                                onClick = {
                                    menuExpanded = false
                                    importLauncher.launch("application/json")
                                },
                            )
                            DropdownMenuItem(
                                text    = { Text("Перевірити оновлення") },
                                onClick = {
                                    menuExpanded = false
                                    vm.checkForUpdate()
                                },
                            )
                        }
                    }
                }
            }
            // ══════════════════════════════════════════════

            // Tab pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface)
                    .padding(4.dp),
            ) {
                TabPill(
                    label       = "Переглянуто",
                    count       = viewed.size,
                    selected    = selectedTab == 0,
                    activeColor = AccentEmerald,
                    modifier    = Modifier.weight(1f),
                    onClick     = { selectedTab = 0 },
                )
                TabPill(
                    label       = "Заплановано",
                    count       = planned.size,
                    selected    = selectedTab == 1,
                    activeColor = AccentIndigo,
                    modifier    = Modifier.weight(1f),
                    onClick     = { selectedTab = 1 },
                )
            }

            if (entries.isEmpty()) {
                EmptyState(
                    icon     = if (selectedTab == 0) Icons.Outlined.Movie else Icons.Outlined.Bookmarks,
                    headline = if (selectedTab == 0) "Ще немає переглядів" else "Список планів порожній",
                    subtitle = if (selectedTab == 0)
                        "Натисни кнопку, щоб додати перший перегляд"
                    else
                        "Натисни кнопку, щоб почати список",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(entries, key = { it.entry.id }) { item ->
                        Box {
                            val card: @Composable () -> Unit = {
                                EntryCard(
                                    item        = item,
                                    onClick     = { onViewEntry(item.entry.id) },
                                    onLongClick = { expandedItemId = item.entry.id },
                                    modifier    = Modifier.fillMaxWidth(),
                                )
                            }
                            if (selectedTab == 1) {
                                SwipeToWatched(onSwipe = { vm.moveToWatched(item.entry) }) { card() }
                            } else {
                                card()
                            }
                            DropdownMenu(
                                expanded         = expandedItemId == item.entry.id,
                                onDismissRequest = { expandedItemId = null },
                            ) {
                                DropdownMenuItem(
                                    text        = { Text("Редагувати") },
                                    leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
                                    onClick     = {
                                        expandedItemId = null
                                        onEditEntry(item.entry.id)
                                    },
                                )
                                DropdownMenuItem(
                                    text        = { Text("Видалити", color = DeleteRed) },
                                    leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = DeleteRed) },
                                    onClick     = {
                                        expandedItemId = null
                                        deleteTarget   = item
                                    },
                                )
                            }
                        }
                    }
                }
            }
            } // Column
        } // Box
    }

    UpdateDialog(
        state     = updateState,
        onDismiss = { vm.resetUpdateState() },
        onInstall = { url -> vm.downloadAndInstall(url) },
    )

    deleteTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title            = { Text("Ви впевнені?", color = TextPrimary) },
            confirmButton    = {
                TextButton(onClick = {
                    vm.delete(target.entry)
                    deleteTarget = null
                }) {
                    Text("Так", color = DeleteRed)
                }
            },
            dismissButton    = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Скасувати")
                }
            },
        )
    }
}

private fun shareBackupFile(context: Context, json: String) {
    val dir  = File(context.cacheDir, "backup").also { it.mkdirs() }
    val file = File(dir, "my_listochok_backup.json")
    file.writeText(json)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Поділитися резервною копією"))
}

private fun DrawScope.drawMagicStar(
    center: Offset,
    outerRadius: Float,
    color: Color,
    alpha: Float = 1f,
) {
    val innerRadius = outerRadius * 0.4f
    val path = Path()
    for (i in 0 until 10) {
        val angle = (i * Math.PI / 5 - Math.PI / 2).toFloat()
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val x = center.x + r * cos(angle)
        val y = center.y + r * sin(angle)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color.copy(alpha = alpha))
}

@Composable
private fun TabPill(
    label: String,
    count: Int,
    selected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) activeColor.copy(alpha = 0.14f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text       = label,
                style      = MaterialTheme.typography.bodyMedium,
                color      = if (selected) activeColor else TextSecondary,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
            if (count > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) activeColor.copy(alpha = 0.18f) else Surface)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) activeColor else TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    headline: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment  = Alignment.CenterHorizontally,
            verticalArrangement  = Arrangement.spacedBy(10.dp),
            modifier             = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint     = TextMuted,
                modifier = Modifier.size(52.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text      = headline,
                style     = MaterialTheme.typography.titleMedium,
                color     = TextSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodySmall,
                color     = TextMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToWatched(
    onSwipe: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onSwipe(); true } else false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
    )
    SwipeToDismissBox(
        state                    = state,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent        = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentEmerald),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(
                    horizontalAlignment  = Alignment.CenterHorizontally,
                    modifier             = Modifier.padding(end = 24.dp),
                    verticalArrangement  = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint     = Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                    Text(
                        text  = "Переглянуто",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                    )
                }
            }
        },
        content = { content() },
    )
}

@Composable
private fun UpdateDialog(
    state: UpdateState,
    onDismiss: () -> Unit,
    onInstall: (apkUrl: String) -> Unit,
) {
    if (state is UpdateState.Idle) return

    val dismissable = state is UpdateState.Available ||
                      state is UpdateState.UpToDate  ||
                      state is UpdateState.Error

    AlertDialog(
        containerColor   = Surface,
        onDismissRequest = { if (dismissable) onDismiss() },
        title = {
            Text(
                text = when (state) {
                    is UpdateState.Checking    -> "Перевірка оновлень"
                    is UpdateState.Available   -> "Доступне оновлення"
                    is UpdateState.Downloading -> "Завантаження"
                    is UpdateState.UpToDate    -> "Версія актуальна"
                    is UpdateState.Error       -> "Помилка"
                    else                       -> ""
                },
                color      = TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            when (state) {
                is UpdateState.Checking -> {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier    = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color       = AccentPrimary,
                        )
                        Text("Зачекайте...", color = TextSecondary)
                    }
                }
                is UpdateState.Available -> {
                    Text("Нова версія: v${state.version}", color = TextSecondary)
                }
                is UpdateState.Downloading -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        LinearProgressIndicator(
                            progress = { state.progress },
                            modifier = Modifier.fillMaxWidth(),
                            color    = AccentPrimary,
                        )
                        Text(
                            text  = "${(state.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                        )
                    }
                }
                is UpdateState.UpToDate -> {
                    Text("У вас встановлена остання версія", color = TextSecondary)
                }
                is UpdateState.Error -> {
                    Text(state.message, color = TextSecondary)
                }
                else -> {}
            }
        },
        confirmButton = {
            when (state) {
                is UpdateState.Available -> TextButton(
                    onClick = { onInstall(state.apkUrl) }
                ) {
                    Text("Встановити", color = AccentPrimary, fontWeight = FontWeight.SemiBold)
                }
                is UpdateState.UpToDate, is UpdateState.Error -> TextButton(
                    onClick = onDismiss
                ) {
                    Text("OK", color = AccentPrimary)
                }
                else -> {}
            }
        },
        dismissButton = {
            if (state is UpdateState.Available) {
                TextButton(onClick = onDismiss) {
                    Text("Пізніше", color = TextSecondary)
                }
            }
        },
    )
}
