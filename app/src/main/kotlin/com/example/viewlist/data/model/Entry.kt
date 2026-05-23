package com.example.viewlist.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val impression: String = "",
    val rating: Float = 0f,
    val imageUrl: String = "",
    val category: String = "Фільм",
    val status: String,
    val watchedAt: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
