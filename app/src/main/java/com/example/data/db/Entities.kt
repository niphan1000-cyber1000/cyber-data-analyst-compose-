package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datasets")
data class DatasetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val type: String, // "CSV" or "SQL"
    val content: String, // CSV raw text
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "analysis_history")
data class AnalysisHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val datasetId: Int,
    val queryText: String, // Thai question
    val responseJson: String, // JSON response containing kpis, charts, insights
    val timestamp: Long = System.currentTimeMillis()
)
