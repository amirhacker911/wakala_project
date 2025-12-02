package com.wakala.fakhr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ResultDao {
    @Insert
    suspend fun insert(result: ResultEntity): Long

    @Query("SELECT * FROM analysis_results WHERE uploaded = 0")
    suspend fun pending(): List<ResultEntity>

    @Update
    suspend fun update(result: ResultEntity)
}
