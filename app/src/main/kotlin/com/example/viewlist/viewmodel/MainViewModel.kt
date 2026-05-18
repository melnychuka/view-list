package com.example.viewlist.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewlist.data.db.AppDatabase
import com.example.viewlist.data.model.Entry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).entryDao()

    val viewed = dao.getEntriesByStatus("viewed")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val planned = dao.getEntriesByStatus("planned")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(entry: Entry) = viewModelScope.launch {
        if (entry.id == 0L) dao.insert(entry) else dao.update(entry)
    }

    fun delete(entry: Entry) = viewModelScope.launch { dao.delete(entry) }

    suspend fun findById(id: Long): Entry? = dao.getById(id)
}
