package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: DatasetEntity): Long

    @Query("DELETE FROM datasets WHERE id = :id")
    suspend fun deleteDataset(id: Int)

    @Query("SELECT * FROM datasets ORDER BY timestamp DESC")
    fun getAllDatasets(): Flow<List<DatasetEntity>>

    @Query("SELECT * FROM datasets WHERE id = :id LIMIT 1")
    suspend fun getDatasetById(id: Int): DatasetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: AnalysisHistoryEntity): Long

    @Query("SELECT * FROM analysis_history WHERE datasetId = :datasetId ORDER BY timestamp DESC")
    fun getAllHistoryForDataset(datasetId: Int): Flow<List<AnalysisHistoryEntity>>

    @Query("SELECT * FROM analysis_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<AnalysisHistoryEntity>>

    @Query("DELETE FROM analysis_history WHERE id = :id")
    suspend fun deleteHistory(id: Int)

    @Query("DELETE FROM analysis_history WHERE datasetId = :datasetId")
    suspend fun clearHistoryForDataset(datasetId: Int)
}
