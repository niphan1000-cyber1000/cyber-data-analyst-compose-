package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class KpiItem(
    val title: String,       // e.g. "ยอดขายรวม"
    val value: String,       // e.g. "120,500 บาท"
    val change: String? = null // e.g. "+12.5%" or "ลดลง 3%"
)

@JsonClass(generateAdapter = true)
data class ChartDataPoint(
    val label: String,       // e.g. "ม.ค." or "สมาร์ทโฟน"
    val value: Double        // e.g. 45000.0
)

@JsonClass(generateAdapter = true)
data class ChartConfig(
    val chartType: String,   // "BAR", "LINE", "PIE", "AREA"
    val title: String,       // e.g. "กราฟแสดงยอดขายแต่ละหมวดหมู่"
    val xAxisLabel: String? = null,
    val yAxisLabel: String? = null,
    val data: List<ChartDataPoint>
)

@JsonClass(generateAdapter = true)
data class AnalysisResult(
    val title: String,
    val kpis: List<KpiItem>,
    val chart: ChartConfig? = null,
    val insights: List<String>,
    val generatedSql: String? = null
)
