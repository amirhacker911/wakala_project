package com.wakala.fakhr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_results")
data class ResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val text: String,
    val prediction: String,
    val uploaded: Boolean = false
)
