package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.AnalysisHistoryEntity
import com.example.data.db.DatasetEntity
import com.example.data.model.AnalysisResult
import com.example.data.model.ChartConfig
import com.example.data.model.DatasetTemplates
import com.example.data.repository.AnalyticsRepository
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AnalysisUiState {
    object Idle : AnalysisUiState
    object Loading : AnalysisUiState
    data class Success(val result: AnalysisResult) : AnalysisUiState
    data class Error(val message: String) : AnalysisUiState
}

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = AnalyticsRepository(db.analyticsDao())

    // Datasets
    val datasets: StateFlow<List<DatasetEntity>> = repository.allDatasets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // History
    val history: StateFlow<List<AnalysisHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _selectedDataset = MutableStateFlow<DatasetEntity?>(null)
    val selectedDataset: StateFlow<DatasetEntity?> = _selectedDataset

    private val _analysisUiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Idle)
    val analysisUiState: StateFlow<AnalysisUiState> = _analysisUiState

    private val _thaiQuery = MutableStateFlow("")
    val thaiQuery: StateFlow<String> = _thaiQuery

    // SQL mock connection simulator
    private val _sqlQuery = MutableStateFlow("SELECT * FROM data LIMIT 10")
    val sqlQuery: StateFlow<String> = _sqlQuery

    private val _sqlExecutionResult = MutableStateFlow<List<List<String>>?>(null)
    val sqlExecutionResult: StateFlow<List<List<String>>?> = _sqlExecutionResult

    init {
        // One-shot seeding using SharedPreferences to avoid auto-reseeding after deleting
        viewModelScope.launch {
            val prefs = application.getSharedPreferences("ai_analytics_prefs", android.content.Context.MODE_PRIVATE)
            val hasSeeded = prefs.getBoolean("has_seeded_templates", false)
            if (!hasSeeded) {
                // Seed templates
                DatasetTemplates.list.forEach { template ->
                    repository.insertDataset(
                        name = template.name,
                        description = template.description,
                        content = template.content,
                        type = template.type
                    )
                }
                prefs.edit().putBoolean("has_seeded_templates", true).apply()
            }
            
            // Now observe datasets normally to handle selection
            repository.allDatasets.collect { list ->
                if (_selectedDataset.value == null && list.isNotEmpty()) {
                    _selectedDataset.value = list.firstOrNull()
                }
            }
        }
    }

    fun selectDataset(dataset: DatasetEntity) {
        _selectedDataset.value = dataset
        _analysisUiState.value = AnalysisUiState.Idle
        _sqlExecutionResult.value = null
    }

    fun updateThaiQuery(query: String) {
        _thaiQuery.value = query
    }

    fun updateSqlQuery(query: String) {
        _sqlQuery.value = query
    }

    // Trigger AI Data analysis using Gemini
    fun runAnalytics() {
        val dataset = _selectedDataset.value ?: return
        val query = _thaiQuery.value.trim()
        if (query.isEmpty()) return

        _analysisUiState.value = AnalysisUiState.Loading

        viewModelScope.launch {
            try {
                val result = repository.analyzeDataWithGemini(
                    datasetName = dataset.name,
                    csvContent = dataset.content,
                    userQuery = query,
                    datasetId = dataset.id
                )
                _analysisUiState.value = AnalysisUiState.Success(result)
            } catch (e: Exception) {
                _analysisUiState.value = AnalysisUiState.Error(
                    e.message ?: "เกิดข้อผิดพลาดในการวิเคราะห์ข้อมูลกรุณาลองใหม่อีกครั้ง"
                )
            }
        }
    }

    // Create a new custom CSV dataset
    fun addNewDataset(name: String, description: String, csvContent: String) {
        viewModelScope.launch {
            val trimmedName = name.trim().ifEmpty { "ชุดข้อมูลใหม่" }
            val trimmedDesc = description.trim().ifEmpty { "ชุดข้อมูลที่ป้อนด้วยตนเอง" }
            val id = repository.insertDataset(trimmedName, trimmedDesc, csvContent, "CSV")
            // Automatically select the new dataset
            val newDataset = DatasetEntity(
                id = id.toInt(),
                name = trimmedName,
                description = trimmedDesc,
                content = csvContent,
                type = "CSV"
            )
            selectDataset(newDataset)
        }
    }

    // Delete a dataset
    fun deleteDataset(id: Int) {
        viewModelScope.launch {
            repository.deleteDataset(id)
            if (_selectedDataset.value?.id == id) {
                _selectedDataset.value = datasets.value.firstOrNull { it.id != id }
                _analysisUiState.value = AnalysisUiState.Idle
            }
        }
    }

    // Clear history for active dataset
    fun clearHistory() {
        val datasetId = _selectedDataset.value?.id ?: return
        viewModelScope.launch {
            repository.clearHistoryForDataset(datasetId)
        }
    }

    // Execute mock SQL simulation
    fun runMockSql() {
        val dataset = _selectedDataset.value ?: return
        val query = _sqlQuery.value.trim().uppercase()

        // Simple CSV parser to represent DB rows
        val lines = dataset.content.split("\n").filter { it.isNotBlank() }
        if (lines.isEmpty()) return

        val headers = lines[0].split(",")
        val rows = lines.drop(1).map { it.split(",") }

        // Mock different SQL queries based on keywords
        val result = mutableListOf<List<String>>()
        result.add(headers) // headers first

        if (query.contains("COUNT") || query.contains("SUM") || query.contains("AVG")) {
            // Aggregate projection mock
            if (query.contains("SUM") && query.contains("ยอดขาย")) {
                val salesColIndex = headers.indexOfFirst { it.contains("ขาย") || it.contains("รวม") || it.contains("จ่าย") }
                if (salesColIndex != -1) {
                    val sum = rows.sumOf { it.getOrNull(salesColIndex)?.toDoubleOrNull() ?: 0.0 }
                    result.add(listOf("SUM(ยอดขายรวม)", sum.toString()))
                } else {
                    result.add(listOf("RESULT", "ไม่พบคอลัมน์ยอดขาย"))
                }
            } else {
                result.add(listOf("COUNT(*)", rows.size.toString()))
            }
        } else if (query.contains("WHERE")) {
            // Basic search filter mock (take first 5 rows that match any query keyword or similar)
            val filteredRows = rows.take(5)
            result.addAll(filteredRows)
        } else {
            // Default SELECT * LIMIT 5
            result.addAll(rows.take(5))
        }

        _sqlExecutionResult.value = result
    }

    // Pre-populate with preset historic analytics if user wants
    fun populateHistoryWithDemoResult(result: AnalysisResult) {
        _analysisUiState.value = AnalysisUiState.Success(result)
    }
}
