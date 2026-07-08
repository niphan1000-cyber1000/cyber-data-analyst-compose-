package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.DatasetEntity
import com.example.data.model.AnalysisResult
import com.example.data.model.ChartConfig
import com.example.data.model.ChartDataPoint
import com.example.data.model.KpiItem
import com.example.ui.components.SmartChartContainer
import com.example.ui.theme.*
import com.example.ui.viewmodel.AnalysisUiState
import com.example.ui.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AnalyticsViewModel) {
    val datasets by viewModel.datasets.collectAsStateWithLifecycle()
    val selectedDataset by viewModel.selectedDataset.collectAsStateWithLifecycle()
    val analysisUiState by viewModel.analysisUiState.collectAsStateWithLifecycle()
    val thaiQuery by viewModel.thaiQuery.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: AI Chat Analytics, 1: Data Manager, 2: SQL Simulator
    var showAddDatasetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Psychology,
                            contentDescription = "AI Dashboard",
                            tint = AccentCyan,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "SYSTEM STATUS: ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = AccentCyan
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "วิเคราะห์ข้อมูล AI อัจฉริยะ",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepSlateSurface,
                    titleContentColor = TextPrimary
                ),
                actions = {
                    IconButton(
                        onClick = { showAddDatasetDialog = true },
                        modifier = Modifier.testTag("action_add_dataset")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AddCircle,
                            contentDescription = "เพิ่มข้อมูลใหม่",
                            tint = AccentCyan
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepSlateSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    icon = { Icon(Icons.Filled.Psychology, "AI Chat", tint = if (activeTab == 0) AccentCyan else TextSecondary) },
                    label = { Text("คุยกับ AI", color = if (activeTab == 0) TextPrimary else TextSecondary) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = LightSlateSurface
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    icon = { Icon(Icons.Filled.Dataset, "Data Manager", tint = if (activeTab == 1) AccentCyan else TextSecondary) },
                    label = { Text("คลังข้อมูล", color = if (activeTab == 1) TextPrimary else TextSecondary) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = LightSlateSurface
                    )
                )
                NavigationBarItem(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    icon = { Icon(Icons.Filled.Terminal, "SQL Simulator", tint = if (activeTab == 2) AccentCyan else TextSecondary) },
                    label = { Text("SQL รันเนอร์", color = if (activeTab == 2) TextPrimary else TextSecondary) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = LightSlateSurface
                    )
                )
            }
        },
        containerColor = ObsidianBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Dropdown selector for Active Dataset (always visible to keep context)
            ActiveDatasetSelector(
                datasets = datasets,
                selected = selectedDataset,
                onSelected = { viewModel.selectDataset(it) },
                onAddClick = { showAddDatasetDialog = true }
            )

            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            // Load screen dynamically based on active bottom tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> ChatAnalyticsTab(
                        viewModel = viewModel,
                        selectedDataset = selectedDataset,
                        uiState = analysisUiState,
                        queryText = thaiQuery
                    )
                    1 -> DataSourcesTab(
                        datasets = datasets,
                        selectedDataset = selectedDataset,
                        onSelected = { viewModel.selectDataset(it) },
                        onDelete = { viewModel.deleteDataset(it) }
                    )
                    2 -> SqlSimulatorTab(
                        viewModel = viewModel,
                        selectedDataset = selectedDataset
                    )
                }
            }
        }
    }

    if (showAddDatasetDialog) {
        AddDatasetDialog(
            onDismiss = { showAddDatasetDialog = false },
            onSave = { name, desc, csv ->
                viewModel.addNewDataset(name, desc, csv)
                showAddDatasetDialog = false
            }
        )
    }
}

@Composable
fun ActiveDatasetSelector(
    datasets: List<DatasetEntity>,
    selected: DatasetEntity?,
    onSelected: (DatasetEntity) -> Unit,
    onAddClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DeepSlateSurface)
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.TableChart,
            contentDescription = "Active Table",
            tint = ChartTeal,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "ตารางข้อมูลใช้งานอยู่:",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = selected?.name ?: "ไม่มีข้อมูลในระบบ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = "Dropdown",
            tint = TextSecondary
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(LightSlateSurface)
                .width(280.dp)
        ) {
            Text(
                text = "เลือกตารางข้อมูลที่ต้องการวิเคราะห์",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = AccentCyan,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))

            datasets.forEach { dataset ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(dataset.name, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text(
                                dataset.description,
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    onClick = {
                        onSelected(dataset)
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            tint = if (selected?.id == dataset.id) AccentCyan else TextSecondary
                        )
                    }
                )
            }
            HorizontalDivider(color = BorderColor.copy(alpha = 0.5f))
            DropdownMenuItem(
                text = { Text("➕ เพิ่มข้อมูล CSV ของตัวเอง", color = AccentCyan, fontWeight = FontWeight.Bold) },
                onClick = {
                    onAddClick()
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun ChatAnalyticsTab(
    viewModel: AnalyticsViewModel,
    selectedDataset: DatasetEntity?,
    uiState: AnalysisUiState,
    queryText: String
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (selectedDataset == null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.FolderOpen,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "กรุณาเลือกตารางข้อมูลเพื่อเริ่มต้น",
                        color = TextSecondary,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            return
        }

        // Suggestions / Prompt Chips
        val suggestions = listOf(
            "วิเคราะห์ยอดขายแยกตามสินค้าและประเภทด้วยกราฟแท่ง",
            "สรุปยอดรวมทั้งหมดและข้อมูลเชิงลึกในตารางนี้",
            "ขอสัดส่วนการกระจายตัวด้วยกราฟวงกลม",
            "สินค้าหรือแหล่งใดมีสัดส่วนดีที่สุดและแย่ที่สุด"
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Intro Guide
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "💡 ตัวอย่างคำสั่งที่คุยกับ AI วิเคราะห์ได้:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        suggestions.forEach { prompt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.updateThaiQuery(prompt) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = ChartTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = prompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Quick Actions
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "⚡ เมนูลัดการวิเคราะห์ (Quick Actions)",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = AccentCyan,
                        letterSpacing = 1.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateThaiQuery("สรุปยอดขายรายสัปดาห์และแนวโน้มการเติบโตเชิงสถิติ")
                                    viewModel.runAnalytics()
                                },
                            colors = CardDefaults.cardColors(containerColor = LightSlateSurface),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("📊 ", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "สรุปรายเดือน",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    viewModel.updateThaiQuery("ช่วยตรวจหาจุดบกพร่อง ข้อผิดพลาด หรือค่าที่ผิดปกติในตารางข้อมูลนี้")
                                    viewModel.runAnalytics()
                                },
                            colors = CardDefaults.cardColors(containerColor = LightSlateSurface),
                            border = BorderStroke(1.dp, BorderColor),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🔍 ", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ค้นหาจุดบกพร่อง",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                // UI States: Idle, Loading, Success, Error
                AnimatedContent(targetState = uiState, label = "AnalysisState") { state ->
                    when (state) {
                        is AnalysisUiState.Idle -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Filled.TipsAndUpdates,
                                        contentDescription = null,
                                        tint = ChartAmber.copy(alpha = 0.8f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "ป้อนคำถามของคุณด้านล่าง แล้วให้ Gemini วิเคราะห์ข้อมูลตัวเลขอัตโนมัติ",
                                        color = TextSecondary,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                        is AnalysisUiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(color = AccentCyan)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "กำลังอ่านชุดข้อมูลและประมวลผลคำนวณสถิติด้วย AI...",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "โมเดล Gemini กำลังสร้างชาร์ตและสรุปเชิงลึก",
                                        color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        is AnalysisUiState.Success -> {
                            AnalysisResultView(result = state.result)
                        }
                        is AnalysisUiState.Error -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0x26EF4444)),
                                border = BorderStroke(1.dp, Color.Red)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Error,
                                        contentDescription = "Error",
                                        tint = Color.Red,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "การวิเคราะห์ล้มเหลว",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = state.message,
                                            color = TextPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat Input Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = DeepSlateSurface,
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = queryText,
                    onValueChange = { viewModel.updateThaiQuery(it) },
                    placeholder = { Text("พิมพ์ถามเรื่อง ยอดขาย, สัดส่วน, ดึงสถิติต่างๆ...", color = TextSecondary) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    singleLine = false,
                    maxLines = 3
                )
                IconButton(
                    onClick = {
                        if (queryText.trim().isEmpty()) {
                            Toast.makeText(context, "กรุณากรอกคำสั่งวิเคราะห์", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.runAnalytics()
                        }
                    },
                    modifier = Modifier
                        .testTag("send_query_button")
                        .clip(CircleShape)
                        .background(AccentCyan)
                        .size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "ส่งข้อมูลวิเคราะห์",
                        tint = ObsidianBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnalysisResultView(result: AnalysisResult) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Header
        Text(
            text = "📊 ${result.title}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )

        // KPI Badges Grid (Row containing Cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            result.kpis.forEach { kpi ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = LightSlateSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = kpi.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = kpi.value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (!kpi.change.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = kpi.change,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (kpi.change.contains("+")) ChartGreen else ChartCoral,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Dynamic Chart Visualizer
        result.chart?.let { chart ->
            SmartChartContainer(config = chart)
        }

        // Insight Bullet Cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TipsAndUpdates,
                        contentDescription = null,
                        tint = ChartAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "สรุปข้อมูลเชิงลึก (AI Insights):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                result.insights.forEach { insight ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚡",
                            modifier = Modifier.padding(end = 8.dp),
                            color = AccentCyan
                        )
                        Text(
                            text = insight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }

        // Simulated SQL query block if present
        if (!result.generatedSql.isNullOrEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF020617)),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "💻 คำสั่ง SQL ที่จำลองดึงข้อมูล:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = ChartTeal
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = result.generatedSql,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = AccentCyan,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
fun DataSourcesTab(
    datasets: List<DatasetEntity>,
    selectedDataset: DatasetEntity?,
    onSelected: (DatasetEntity) -> Unit,
    onDelete: (Int) -> Unit
) {
    var expandedDatasetId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "📁 จัดการตารางข้อมูลทั้งหมด",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = AccentCyan,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(datasets) { dataset ->
            val isActive = selectedDataset?.id == dataset.id
            val isExpanded = expandedDatasetId == dataset.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (isActive) AccentCyan else BorderColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isActive) DeepSlateSurface else DeepSlateSurface.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Storage,
                            contentDescription = null,
                            tint = if (isActive) AccentCyan else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = dataset.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                if (isActive) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ChartTeal)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "เปิดอยู่",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = ObsidianBg,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Text(
                                text = dataset.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        IconButton(onClick = {
                            expandedDatasetId = if (isExpanded) null else dataset.id
                        }) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = "พรีวิว",
                                tint = TextSecondary
                            )
                        }
                    }

                    // Bottom controls (Select or Delete)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (!isActive) {
                            TextButton(
                                onClick = { onSelected(dataset) },
                                colors = ButtonDefaults.textButtonColors(contentColor = AccentCyan)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("เปิดใช้งาน", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        // Disable deleting the preloaded ones (ID <= 4)
                        if (dataset.id > 4) {
                            TextButton(
                                onClick = { onDelete(dataset.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = ChartCoral)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ลบออก")
                            }
                        }
                    }

                    // Expand table preview
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .background(Color(0xFF0B1220))
                                .border(1.dp, BorderColor, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🔍 พรีวิวข้อมูลดิบ (Raw CSV):",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = ChartTeal,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // CSV Data Grid Table representation
                            CsvDataGridTable(csvContent = dataset.content)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CsvDataGridTable(csvContent: String) {
    val scrollState = rememberScrollState()
    val lines = csvContent.split("\n").filter { it.isNotBlank() }
    if (lines.isEmpty()) {
        Text("ตารางว่างเปล่า", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
        return
    }

    val headers = lines[0].split(",")
    val rows = lines.drop(1).map { it.split(",") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
    ) {
        // Table Headers
        Row(
            modifier = Modifier
                .background(LightSlateSurface)
                .padding(vertical = 6.dp)
        ) {
            headers.forEach { header ->
                Text(
                    text = header,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan,
                    modifier = Modifier
                        .width(110.dp)
                        .padding(horizontal = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Table Rows
        rows.take(15).forEachIndexed { index, row ->
            Row(
                modifier = Modifier
                    .background(if (index % 2 == 0) Color.Transparent else DeepSlateSurface.copy(alpha = 0.3f))
                    .padding(vertical = 6.dp)
            ) {
                headers.indices.forEach { colIndex ->
                    val cellText = row.getOrNull(colIndex) ?: ""
                    Text(
                        text = cellText,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        modifier = Modifier
                            .width(110.dp)
                            .padding(horizontal = 8.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
        if (rows.size > 15) {
            Text(
                text = "... และอีก ${rows.size - 15} แถวข้อมูล",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun SqlSimulatorTab(
    viewModel: AnalyticsViewModel,
    selectedDataset: DatasetEntity?
) {
    val sqlQuery by viewModel.sqlQuery.collectAsStateWithLifecycle()
    val executionResult by viewModel.sqlExecutionResult.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "💻 ตัวประมวลผลคำสั่ง SQL (SQL Query Simulator)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = AccentCyan
        )

        Text(
            text = "พิมพ์คำสั่ง SQL เช่น SELECT, COUNT, SUM ยอดรวมด้านล่าง เพื่อจำลองการเชื่อมต่อฐานข้อมูล SQL ในแอป",
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )

        // Query input box
        OutlinedTextField(
            value = sqlQuery,
            onValueChange = { viewModel.updateSqlQuery(it) },
            label = { Text("คำสั่ง SQL", color = AccentCyan) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sql_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentCyan,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = DeepSlateSurface,
                unfocusedContainerColor = DeepSlateSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
            maxLines = 4
        )

        // Suggestion queries
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sqlPresets = listOf(
                "SELECT * FROM data" to "ดึงข้อมูลทั้งหมด",
                "SELECT SUM(ยอดขาย) FROM data" to "รวมยอดขายทั้งหมด"
            )
            sqlPresets.forEach { preset ->
                AssistChip(
                    onClick = { viewModel.updateSqlQuery(preset.first) },
                    label = { Text(preset.second, color = TextPrimary) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = LightSlateSurface)
                )
            }
        }

        Button(
            onClick = { viewModel.runMockSql() },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("run_sql_button"),
            colors = ButtonDefaults.buttonColors(containerColor = AccentCyan, contentColor = ObsidianBg)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ประมวลผล SQL", fontWeight = FontWeight.Bold)
        }

        // Execution Result Output Table
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
            border = BorderStroke(1.dp, BorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🖥️ ผลลัพธ์ข้อมูล (Output Table):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ChartTeal,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (executionResult == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ยังไม่มีข้อมูลผลลัพธ์ กดปุ่มประมวลผลเพื่อคำนวณ",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(scrollState)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Headers
                        val headers = executionResult!!.firstOrNull() ?: emptyList()
                        Row(
                            modifier = Modifier
                                .background(LightSlateSurface)
                                .padding(vertical = 6.dp)
                        ) {
                            headers.forEach { header ->
                                Text(
                                    text = header,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentCyan,
                                    modifier = Modifier
                                        .width(120.dp)
                                        .padding(horizontal = 8.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Rows
                        executionResult!!.drop(1).forEachIndexed { rowIndex, row ->
                            Row(
                                modifier = Modifier
                                    .background(if (rowIndex % 2 == 0) Color.Transparent else DeepSlateSurface.copy(alpha = 0.2f))
                                    .padding(vertical = 6.dp)
                            ) {
                                headers.indices.forEach { colIndex ->
                                    val cellText = row.getOrNull(colIndex) ?: ""
                                    Text(
                                        text = cellText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary,
                                        modifier = Modifier
                                            .width(120.dp)
                                            .padding(horizontal = 8.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDatasetDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var csvContent by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepSlateSurface),
            border = BorderStroke(1.dp, BorderColor),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "➕ เพิ่มชุดข้อมูลใหม่ (CSV)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )

                Text(
                    text = "ป้อนข้อมูลคั่นด้วยเครื่องหมายจุลภาค (,) และบรรทัดแรกระบุชื่อแกนหลัก (Headers)",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ชื่อชุดข้อมูล", color = AccentCyan) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_dataset_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderColor
                    )
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("คำอธิบายสั้นๆ", color = AccentCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderColor
                    )
                )

                OutlinedTextField(
                    value = csvContent,
                    onValueChange = { csvContent = it },
                    label = { Text("ข้อมูลดิบ CSV", color = AccentCyan) },
                    placeholder = {
                        Text(
                            "เช่น:\nปี,ยอดขาย,กำไร\n2024,45000,9000\n2025,60000,12000",
                            color = TextSecondary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .testTag("dialog_dataset_csv"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = BorderColor
                    ),
                    maxLines = 15,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("ยกเลิก", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (csvContent.isNotBlank()) {
                                onSave(name, description, csvContent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentCyan,
                            contentColor = ObsidianBg
                        ),
                        modifier = Modifier.testTag("dialog_save_button")
                    ) {
                        Text("บันทึกข้อมูล", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
