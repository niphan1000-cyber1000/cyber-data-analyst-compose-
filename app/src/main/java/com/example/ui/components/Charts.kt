package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChartConfig
import com.example.data.model.ChartDataPoint
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SmartChartContainer(
    config: ChartConfig,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chart_card"),
        colors = CardDefaults.cardColors(
            containerColor = DeepSlateSurface
        ),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            // Chart Title
            Text(
                text = config.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AccentCyan,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (config.data.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ไม่มีข้อมูลสำหรับแสดงกราฟ",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    when (config.chartType.uppercase()) {
                        "BAR" -> CustomBarChart(data = config.data)
                        "LINE" -> CustomLineChart(data = config.data, isArea = false)
                        "AREA" -> CustomLineChart(data = config.data, isArea = true)
                        "PIE" -> CustomPieChart(data = config.data)
                        else -> CustomBarChart(data = config.data) // Fallback
                    }
                }

                // Axis Labels
                if ((config.xAxisLabel != null || config.yAxisLabel != null) && config.chartType.uppercase() != "PIE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (config.xAxisLabel != null) {
                            Text(
                                text = "แนวนอน: ${config.xAxisLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        if (config.yAxisLabel != null) {
                            Text(
                                text = "แนวตั้ง: ${config.yAxisLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBarChart(data: List<ChartDataPoint>) {
    val maxVal = (data.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        val colors = listOf(ChartTeal, ChartGreen, ChartCoral, ChartAmber, ChartPurple, ChartBlue)

        data.forEachIndexed { index, item ->
            val color = colors[index % colors.size]
            val barHeightPercent = (item.value / maxVal).toFloat()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Value text on top of bar
                Text(
                    text = formatShortNumber(item.value),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // The animated Bar Column
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(barHeightPercent * 0.85f * animationProgress.value)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    color,
                                    color.copy(alpha = 0.4f)
                                )
                            )
                        )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // X Axis label
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = TextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CustomLineChart(data: List<ChartDataPoint>, isArea: Boolean) {
    val maxVal = (data.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val primaryColor = ChartTeal
    val areaGradient = Brush.verticalGradient(
        colors = listOf(primaryColor.copy(alpha = 0.35f), Color.Transparent)
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val pointsCount = data.size
                if (pointsCount < 1) return@Canvas

                val stepX = width / (if (pointsCount > 1) pointsCount - 1 else 1)

                // Draw Grid Lines (horizontal)
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = height * i / gridLines
                    drawLine(
                        color = BorderColor.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                val path = Path()
                val areaPath = Path()

                data.forEachIndexed { index, item ->
                    val x = index * stepX
                    val yPercent = (item.value / maxVal).toFloat()
                    val y = height - (yPercent * height * 0.8f * animationProgress.value) - (height * 0.05f)

                    if (index == 0) {
                        path.moveTo(x, y)
                        if (isArea) {
                            areaPath.moveTo(x, height)
                            areaPath.lineTo(x, y)
                        }
                    } else {
                        // Smooth curves using bezier
                        val prevX = (index - 1) * stepX
                        val prevYPercent = (data[index - 1].value / maxVal).toFloat()
                        val prevY = height - (prevYPercent * height * 0.8f * animationProgress.value) - (height * 0.05f)

                        path.cubicTo(
                            (prevX + x) / 2, prevY,
                            (prevX + x) / 2, y,
                            x, y
                        )
                        if (isArea) {
                            areaPath.cubicTo(
                                (prevX + x) / 2, prevY,
                                (prevX + x) / 2, y,
                                x, y
                            )
                        }
                    }

                    if (index == pointsCount - 1 && isArea) {
                        areaPath.lineTo(x, height)
                        areaPath.close()
                    }
                }

                // Draw Area Fill
                if (isArea && pointsCount > 0) {
                    drawPath(
                        path = areaPath,
                        brush = areaGradient
                    )
                }

                // Draw Main Trend Line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw Dots & Values
                data.forEachIndexed { index, item ->
                    val x = index * stepX
                    val yPercent = (item.value / maxVal).toFloat()
                    val y = height - (yPercent * height * 0.8f * animationProgress.value) - (height * 0.05f)

                    // Dot
                    drawCircle(
                        color = AccentCyan,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = ObsidianBg,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            data.forEachIndexed { index, item ->
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(48.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CustomPieChart(data: List<ChartDataPoint>) {
    val totalSum = data.sumOf { it.value }.coerceAtLeast(1.0)
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )
    }

    val colors = listOf(ChartTeal, ChartGreen, ChartCoral, ChartAmber, ChartPurple, ChartBlue)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie Canvas
        Box(
            modifier = Modifier
                .weight(1.1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(170.dp)) {
                var startAngle = -90f

                data.forEachIndexed { index, item ->
                    val sweepAngle = ((item.value / totalSum) * 360f).toFloat() * animationProgress.value
                    val color = colors[index % colors.size]

                    // Draw slice
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 30.dp.toPx(), cap = StrokeCap.Butt)
                    )

                    startAngle += sweepAngle
                }
            }

            // Center Text inside Donut
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ทั้งหมด",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = formatShortNumber(totalSum),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        // Legend list (Max 5 items shown beautifully)
        Column(
            modifier = Modifier
                .weight(0.9f)
                .fillMaxHeight()
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            data.take(5).forEachIndexed { index, item ->
                val color = colors[index % colors.size]
                val percent = ((item.value / totalSum) * 100).toInt()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${formatShortNumber(item.value)} ($percent%)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = TextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            if (data.size > 5) {
                Text(
                    text = "+ อีก ${data.size - 5} รายการ",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 18.dp, top = 4.dp)
                )
            }
        }
    }
}

// Helper to format short business numbers
fun formatShortNumber(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format("%.1fM", value / 1_000_000.0)
        value >= 1_000 -> String.format("%.1fK", value / 1_000.0)
        else -> {
            if (value % 1.0 == 0.0) {
                value.toInt().toString()
            } else {
                String.format("%.1f", value)
            }
        }
    }
}
