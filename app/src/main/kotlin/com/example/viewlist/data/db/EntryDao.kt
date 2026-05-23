package com.example.viewlist.data.db

import androidx.room.*
import com.example.viewlist.data.model.Entry
import com.example.viewlist.data.model.EntryWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {

    @Transaction
    @Query("SELECT * FROM entries WHERE status = :status ORDER BY createdAt DESC")
    fun getWithGenresByStatus(status: String): Flow<List<EntryWithGenres>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Entry?

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getWithGenresById(id: Long): EntryWithGenres?

    @Query("SELECT * FROM entries")
    suspend fun getAll(): List<Entry>

    @Query("SELECT * FROM entries WHERE createdAt = :createdAt LIMIT 1")
    suspend fun findByCreatedAt(createdAt: Long): Entry?
}
