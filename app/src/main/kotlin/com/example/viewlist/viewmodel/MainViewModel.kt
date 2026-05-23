package com.example.viewlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewlist.data.backup.BackupManager
import com.example.viewlist.data.db.AppDatabase
import com.example.viewlist.data.model.Entry
import com.example.viewlist.data.model.EntryGenreCrossRef
import com.example.viewlist.data.model.EntryWithGenres
import com.example.viewlist.data.model.Genre
import com.example.viewlist.data.update.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val db            = AppDatabase.get(app)
    private val entryDao      = db.entryDao()
    private val genreDao      = db.genreDao()
    private val backupManager = BackupManager(db, app)
    private val updateManager = UpdateManager(app)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    val viewed: StateFlow<List<EntryWithGenres>> = entryDao
        .getWithGenresByStatus("viewed")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val planned: StateFlow<List<EntryWithGenres>> = entryDao
        .getWithGenresByStatus("planned")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveWithGenres(entry: Entry, genreNames: List<String>, onDone: () -> Unit) {
        viewModelScope.launch {
            val entryId = if (entry.id == 0L) entryDao.insert(entry) else {
                entryDao.update(entry)
                entry.id
            }
            genreDao.clearForEntry(entryId)
            genreNames.map { it.trim() }.filter { it.isNotBlank() }.distinct().forEach { name ->
                val genreId = genreDao.getOrCreate(name)
                genreDao.insertCrossRef(EntryGenreCrossRef(entryId, genreId))
            }
            onDone()
        }
    }

    fun moveToWatched(entry: Entry) = viewModelScope.launch {
        entryDao.update(entry.copy(status = "viewed", watchedAt = System.currentTimeMillis()))
    }

    fun delete(entry: Entry) = viewModelScope.launch { entryDao.delete(entry) }

    suspend fun findById(id: Long): Entry? = entryDao.getById(id)

    suspend fun findWithGenresById(id: Long): EntryWithGenres? = entryDao.getWithGenresById(id)

    suspend fun searchGenres(query: String): List<Genre> = genreDao.search(query)

    suspend fun getGenresForEntry(entryId: Long): List<Genre> = genreDao.getForEntry(entryId)

    fun checkForUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            _updateState.value = UpdateState.Checking
            try {
                val ctx     = getApplication<Application>()
                val current = ctx.packageManager
                    .getPackageInfo(ctx.packageName, 0).versionName ?: "0"
                val info = updateManager.checkForUpdate(current)
                _updateState.value = if (info != null)
                    UpdateState.Available(info.version, info.apkUrl)
                else
                    UpdateState.UpToDate
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Помилка перевірки")
            }
        }
    }

    fun downloadAndInstall(apkUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = updateManager.downloadApk(apkUrl) { progress ->
                    _updateState.value = UpdateState.Downloading(progress)
                }
                updateManager.triggerInstall(file)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Помилка завантаження")
            }
        }
    }

    fun resetUpdateState() { _updateState.value = UpdateState.Idle }

    fun exportJson(onJson: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupManager.exportToJson()
            onJson(json)
        }
    }

    fun importJson(
        json: String,
        onResult: (imported: Int, skipped: Int) -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                val result = backupManager.importFromJson(json)
                onResult(result.imported, result.skipped)
            } catch (e: Exception) {
                onError(e.message ?: "Помилка імпорту")
            }
        }
    }
}
