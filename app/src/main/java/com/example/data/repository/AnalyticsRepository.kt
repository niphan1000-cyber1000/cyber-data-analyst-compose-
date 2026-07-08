package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.GenerationConfig
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.AnalyticsDao
import com.example.data.db.AnalysisHistoryEntity
import com.example.data.db.DatasetEntity
import com.example.data.model.AnalysisResult
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AnalyticsRepository(
    private val dao: AnalyticsDao,
    private val moshi: Moshi = RetrofitClient.moshiInstance
) {
    val allDatasets: Flow<List<DatasetEntity>> = dao.getAllDatasets()
    val allHistory: Flow<List<AnalysisHistoryEntity>> = dao.getAllHistory()

    fun getHistoryForDataset(datasetId: Int): Flow<List<AnalysisHistoryEntity>> =
        dao.getAllHistoryForDataset(datasetId)

    suspend fun getDatasetById(id: Int): DatasetEntity? = withContext(Dispatchers.IO) {
        dao.getDatasetById(id)
    }

    suspend fun insertDataset(name: String, description: String, content: String, type: String = "CSV"): Long =
        withContext(Dispatchers.IO) {
            dao.insertDataset(
                DatasetEntity(
                    name = name,
                    description = description,
                    content = content,
                    type = type
                )
            )
        }

    suspend fun deleteDataset(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteDataset(id)
    }

    suspend fun deleteHistory(id: Int) = withContext(Dispatchers.IO) {
        dao.deleteHistory(id)
    }

    suspend fun clearHistoryForDataset(datasetId: Int) = withContext(Dispatchers.IO) {
        dao.clearHistoryForDataset(datasetId)
    }

    suspend fun analyzeDataWithGemini(
        datasetName: String,
        csvContent: String,
        userQuery: String,
        datasetId: Int
    ): AnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            throw IllegalStateException("กรุณาตั้งค่า GEMINI_API_KEY ในแผง Secrets ของ AI Studio")
        }

        val prompt = """
            วิเคราะห์ชุดข้อมูลต่อไปนี้และตอบคำถามของผู้ใช้
            
            [ชื่อชุดข้อมูล]: $datasetName
            
            [ข้อมูลดิบในรูปแบบ CSV]:
            $csvContent
            
            [คำถามผู้ใช้]: $userQuery
            
            กรุณาคำนวณและประเมินผลตัวเลขตามจริงจากข้อมูลดิบที่ให้มา (ห้ามมโนหรือสุ่มตัวเลขมั่วๆ ให้ทำการคำนวณบวก ลบ คูณ หาร เฉลี่ย ยอดรวม ตามข้อเท็จจริงใน CSV) 
            และตอบกลับมาเป็นโครงสร้าง JSON ตามที่กำหนดไว้เท่านั้น
        """.trimIndent()

        val systemInstruction = """
            คุณคือผู้เชี่ยวชาญด้านการวิเคราะห์ข้อมูลและสร้างแดชบอร์ดอัจฉริยะ (AI Data Analyst)
            หน้าที่ของคุณคือ วิเคราะห์ข้อมูล CSV ที่ผู้ใช้ระบุอย่างละเอียด คำนวณหาค่าสถิติจริง และตอบข้อซักถามของผู้ใช้ด้วยภาษาไทยที่เป็นมืออาชีพ โดยจัดทำโครงสร้างข้อมูลให้อยู่ในรูปแบบ JSON ตาม Schema นี้เท่านั้น:
            {
              "title": "หัวข้อวิเคราะห์สั้นๆ ที่เหมาะสมและครอบคลุมเป็นภาษาไทย",
              "kpis": [
                {
                  "title": "หัวข้อ KPI เป็นภาษาไทย (เช่น ยอดขายรวมทั้งหมด, อัตราความชำรุดเฉลี่ย)",
                  "value": "ค่าของ KPI ที่คำนวณเสร็จแล้วพร้อมหน่วย (เช่น 450,200 บาท, 25 ครั้ง, 4.29%)",
                  "change": "ข้อความสั้นๆ ระบุแนวโน้มหรือการเปรียบเทียบ เช่น +12% จากกุมภาพันธ์, หรือเว้นเป็น null หากไม่มีคู่เปรียบเทียบ"
                }
              ],
              "chart": {
                "chartType": "BAR" (สำหรับกราฟแท่งเปรียบเทียบ), "LINE" (สำหรับแนวโน้มเวลา), "PIE" (สำหรับสัดส่วนเปอร์เซ็นต์), หรือ "AREA" (สำหรับสะสมหรือแนวโน้มพื้นที่),
                "title": "ชื่อของกราฟวิเคราะห์เป็นภาษาไทย",
                "xAxisLabel": "ป้ายแกน X เป็นภาษาไทยหรือ null",
                "yAxisLabel": "ป้ายแกน Y เป็นภาษาไทยหรือ null",
                "data": [
                  {
                    "label": "ป้ายกำกับจุดข้อมูลเป็นภาษาไทย (เช่น ม.ค., สมาร์ทโฟน, Google Search)",
                    "value": ตัวเลขทศนิยมที่คำนวณได้จริง (เช่น 125000.0)
                  }
                ]
              },
              "insights": [
                "ข้อมูลเชิงลึกสรุปที่ได้จากการคำนวณ 1 (เช่น สินค้าประเภทสมาร์ทโฟนทำยอดขายได้สูงสุดถึง 45% ของรายได้ทั้งหมด)",
                "ข้อมูลเชิงลึกสรุปที่ได้จากการคำนวณ 2 (เช่น แนะนำให้ลดงบโฆษณาในช่องทาง Facebook Ads เนื่องจากมีอัตรา Conversion ต่ำที่สุด)"
              ],
              "generatedSql": "คำสั่ง SQL จำลองที่เกี่ยวข้องในการค้นหาข้อมูลนี้ (เช่น SELECT category, SUM(sales) FROM sales GROUP BY category ORDER BY sales DESC)"
            }
            
            ข้อกำหนดที่สำคัญมาก:
            1. ตอบกลับมาเฉพาะข้อความ JSON แท้ๆ เท่านั้น ห้ามใส่โค้ดบล็อก Markdown (ห้ามใช้ ```json ... ```)
            2. ข้อมูลสถิติ ค่ารวม ค่าเฉลี่ย และกราฟ จะต้องตรงกับข้อมูลดิบที่คำนวณได้จริงจากไฟล์ CSV
            3. เขียนภาษาไทยให้อ่านง่าย สรุปชัดเจน และเป็นประโยชน์ในเชิงธุรกิจ
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstruction))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2f // low temperature for precise calculations
            )
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val rawResponseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw IllegalStateException("ไม่ได้รับการตอบสนองจากโมเดลวิเคราะห์ AI")

            // Clean any potential markdown wrapping
            val responseText = cleanJsonString(rawResponseText)

            // Parse response json using Moshi
            val adapter = moshi.adapter(AnalysisResult::class.java)
            val result = adapter.fromJson(responseText)
                ?: throw IllegalStateException("โครงสร้างข้อมูลผลลัพธ์ไม่ถูกต้อง: $responseText")

            // Save this analysis into the database history
            dao.insertHistory(
                AnalysisHistoryEntity(
                    datasetId = datasetId,
                    queryText = userQuery,
                    responseJson = responseText
                )
            )

            result
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun cleanJsonString(input: String): String {
        var cleaned = input.trim()
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replace(Regex("^```(?:json)?"), "").trim()
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substringBeforeLast("```").trim()
        }
        return cleaned
    }
}
