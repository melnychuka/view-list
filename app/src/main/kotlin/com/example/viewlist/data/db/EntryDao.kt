package com.example.viewlist.data.db

import androidx.room.*
import com.example.viewlist.data.model.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries WHERE status = :status ORDER BY createdAt DESC")
    fun getEntriesByStatus(status: String): Flow<List<Entry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: Entry)

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Entry?
}
