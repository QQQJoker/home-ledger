package com.joker.homeledger.feature.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.joker.homeledger.core.common.MoneyFormatter
import com.joker.homeledger.core.common.StatsScope
import com.joker.homeledger.core.model.CategoryExpenseItem
import com.joker.homeledger.core.model.TrendPoint
import com.joker.homeledger.ui.components.StatsPeriodPanel
import kotlin.math.max

@Composable
fun StatsScreen(
    onAddEntryClick: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val pieData = remember(uiState.categoryRank) { buildPieSlices(uiState.categoryRank) }
    val showTrend = uiState.period.scope == StatsScope.YEAR || uiState.period.scope == StatsScope.ALL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("统计", style = MaterialTheme.typography.headlineSmall)
        StatsPeriodPanel(
            value = uiState.period,
            onChange = viewModel::updatePeriod
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("汇总 · ${uiState.period.periodTitle()}", style = MaterialTheme.typography.titleMedium)
                Text("收入: ¥${MoneyFormatter.centToYuan(uiState.summary.incomeCent)}")
                Text("支出: ¥${MoneyFormatter.centToYuan(uiState.summary.expenseCent)}")
                Text("结余: ¥${MoneyFormatter.centToYuan(uiState.summary.balanceCent)}")
            }
        }

        if (uiState.summary.incomeCent == 0L && uiState.summary.expenseCent == 0L) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("当前周期暂无数据")
                    Button(onClick = onAddEntryClick, modifier = Modifier.fillMaxWidth()) {
                        Text("记第一笔")
                    }
                }
            }
        } else {
            ChartCard(title = "支出分类占比") {
                if (pieData.isEmpty()) {
                    Text("暂无支出分类数据")
                } else {
                    PieChart(slices = pieData, modifier = Modifier.fillMaxWidth().height(180.dp))
                    pieData.forEach {
                        Text("${it.label}: ¥${MoneyFormatter.centToYuan(it.valueCent)}")
                    }
                }
            }

            if (showTrend) {
                val trendTitle = when (uiState.period.scope) {
                    StatsScope.YEAR -> "收支趋势（按月）"
                    StatsScope.ALL -> "收支趋势（按年）"
                    else -> "收支趋势"
                }
                ChartCard(title = trendTitle) {
                    if (uiState.trend.isEmpty() || uiState.trend.all { it.incomeCent == 0L && it.expenseCent == 0L }) {
                        Text("暂无趋势数据")
                    } else {
                        TrendLineChart(
                            points = uiState.trend,
                            xLabel = { bucket -> formatTrendLabel(bucket, uiState.period.scope) },
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("● 收入", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
                            Text("● 支出", color = Color(0xFFC62828), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

private fun formatTrendLabel(bucket: String, scope: StatsScope): String {
    return when (scope) {
        StatsScope.YEAR -> {
            val month = bucket.substringAfter('-', bucket).toIntOrNull() ?: return bucket
            "${month}月"
        }
        StatsScope.ALL -> "${bucket}年"
        StatsScope.MONTH -> bucket
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

private data class PieSlice(val label: String, val valueCent: Long, val color: Color)

private fun buildPieSlices(items: List<CategoryExpenseItem>, topN: Int = 5): List<PieSlice> {
    if (items.isEmpty()) return emptyList()
    val colors = listOf(
        Color(0xFFE57373), Color(0xFF64B5F6), Color(0xFF81C784),
        Color(0xFFFFB74D), Color(0xFFBA68C8), Color(0xFF90A4AE)
    )
    val sorted = items.sortedByDescending { it.totalCent }
    val top = sorted.take(topN)
    val other = sorted.drop(topN).sumOf { it.totalCent }
    val result = top.mapIndexed { index, item ->
        PieSlice(item.categoryName, item.totalCent, colors[index % colors.size])
    }.toMutableList()
    if (other > 0L) {
        result.add(PieSlice("其他", other, colors.last()))
    }
    return result
}

@Composable
private fun PieChart(slices: List<PieSlice>, modifier: Modifier = Modifier) {
    val total = slices.sumOf { it.valueCent }.toFloat().coerceAtLeast(1f)
    Canvas(modifier = modifier) {
        var startAngle = -90f
        slices.forEach { slice ->
            val sweep = 360f * (slice.valueCent / total)
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = true,
                size = Size(size.minDimension, size.minDimension),
                topLeft = Offset(
                    (size.width - size.minDimension) / 2f,
                    (size.height - size.minDimension) / 2f
                )
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun TrendLineChart(
    points: List<TrendPoint>,
    xLabel: (String) -> String,
    modifier: Modifier = Modifier
) {
    val labelHeight = 36.dp
  Canvas(modifier = modifier) {
        val bottomPad = labelHeight.toPx()
        val chartHeight = size.height - bottomPad
        val maxValue = points.maxOf { max(it.incomeCent, it.expenseCent) }.coerceAtLeast(1L).toFloat()
        val stepX = if (points.size <= 1) 0f else size.width / (points.size - 1)

        fun yFor(value: Long): Float =
            chartHeight - (value / maxValue) * chartHeight

        val incomePath = Path()
        val expensePath = Path()
        points.forEachIndexed { index, point ->
            val x = stepX * index
            val incomeY = yFor(point.incomeCent)
            val expenseY = yFor(point.expenseCent)
            if (index == 0) {
                incomePath.moveTo(x, incomeY)
                expensePath.moveTo(x, expenseY)
            } else {
                incomePath.lineTo(x, incomeY)
                expensePath.lineTo(x, expenseY)
            }
            drawCircle(Color(0xFF2E7D32), radius = 4f, center = Offset(x, incomeY))
            drawCircle(Color(0xFFC62828), radius = 4f, center = Offset(x, expenseY))
        }
        drawPath(incomePath, Color(0xFF2E7D32), style = Stroke(width = 2f))
        drawPath(expensePath, Color(0xFFC62828), style = Stroke(width = 2f))

        val paint = android.graphics.Paint().apply {
            textSize = 10.sp.toPx()
            color = android.graphics.Color.GRAY
            textAlign = android.graphics.Paint.Align.CENTER
        }
        val nativeCanvas = drawContext.canvas.nativeCanvas
        points.forEachIndexed { index, point ->
            val x = stepX * index
            nativeCanvas.drawText(
                xLabel(point.bucket),
                x,
                size.height - 8f,
                paint
            )
        }
    }
}
