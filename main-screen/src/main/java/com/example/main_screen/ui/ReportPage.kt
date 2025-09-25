package com.example.main_screen.ui

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportPage(navController: NavController) {
    var selectedTab by remember { mutableStateOf("Order") } // "Order" or "Sales"
    var selectedPeriod by remember { mutableStateOf("Year") } // "Year" or "Month"
    var chartEntries by remember { mutableStateOf<List<Pair<Float, Float>>>(emptyList()) }
    var xLabels by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var noData by remember { mutableStateOf(false) }

    val db = FirebaseFirestore.getInstance()

    // 当 tab/period 变化时，重新拉取数据
    LaunchedEffect(selectedTab, selectedPeriod) {
        isLoading = true
        noData = false
        try {
            if (selectedTab == "Order") {
                if (selectedPeriod == "Year") {
                    // 最近 5 年（包含今年），按年份统计订单数量
                    val now = Calendar.getInstance()
                    val currentYear = now.get(Calendar.YEAR)
                    val years = (currentYear - 4..currentYear).toList()
                    val entries = mutableListOf<Pair<Float, Float>>()
                    val labels = mutableListOf<String>()
                    years.forEachIndexed { idx, y ->
                        val count = getOrderCountForYear(db, y)
                        entries.add(idx.toFloat() to count.toFloat())
                        labels.add(y.toString())
                    }
                    chartEntries = entries
                    xLabels = labels
                    noData = entries.all { it.second == 0f }
                } else {
                    // 本年 12 个月
                    val months = (1..12).toList()
                    val entries = months.mapIndexed { idx, m ->
                        idx.toFloat() to getOrderCountForMonth(db, m).toFloat()
                    }
                    chartEntries = entries
                    xLabels = months.map { monthNumberToLabel(it) }
                    noData = chartEntries.all { it.second == 0f }
                }
            } else {
                // Sales
                if (selectedPeriod == "Year") {
                    val now = Calendar.getInstance()
                    val currentYear = now.get(Calendar.YEAR)
                    val years = (currentYear - 4..currentYear).toList()
                    val entries = mutableListOf<Pair<Float, Float>>()
                    val labels = mutableListOf<String>()
                    years.forEachIndexed { idx, y ->
                        val total = getSalesTotalForYear(db, y)
                        entries.add(idx.toFloat() to total.toFloat())
                        labels.add(y.toString())
                    }
                    chartEntries = entries
                    xLabels = labels
                    noData = entries.all { it.second == 0f }
                } else {
                    val months = (1..12).toList()
                    val entries = months.mapIndexed { idx, m ->
                        idx.toFloat() to getSalesTotalForMonth(db, m).toFloat()
                    }
                    chartEntries = entries
                    xLabels = months.map { monthNumberToLabel(it) }
                    noData = chartEntries.all { it.second == 0f }
                }
            }
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 顶部切换按钮： Orders / Sales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ToggleButton(text = "Orders", selected = selectedTab == "Order") { selectedTab = "Order" }
            ToggleButton(text = "Sales", selected = selectedTab == "Sales") { selectedTab = "Sales" }
        }

        // 统计卡（包含 Year/Month 按钮 + 折线图）
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (selectedTab == "Order") "📦 Orders Report" else "💰 Sales Report",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Year / Month 切换按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ToggleSmall(text = "Year", selected = selectedPeriod == "Year") { selectedPeriod = "Year" }
                    ToggleSmall(text = "Month", selected = selectedPeriod == "Month") { selectedPeriod = "Month" }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (isLoading) {
                    CircularProgressIndicator()
                } else if (noData) {
                    // nicer no-data placeholder
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
                        Text("No data for selected range", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                } else {
                    LineChartView(
                        dataPoints = chartEntries,
                        xLabels = xLabels,
                        label = if (selectedTab == "Order") "Orders" else "Sales (RM)",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                    )
                }
            }
        }
    }
}

/* ---------------- UI 小组件 ---------------- */
@Composable
private fun ToggleButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 120.dp)
    ) {
        Text(text)
    }
}

@Composable
private fun ToggleSmall(text: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
            contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.height(36.dp)
    ) {
        Text(text)
    }
}

/* ---------------- 折线图 Composable（MPAndroidChart） ---------------- */
@Composable
fun LineChartView(
    dataPoints: List<Pair<Float, Float>>,
    xLabels: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val onSurface = MaterialTheme.colorScheme.onSurface.toArgb()
    val surfaceColor = MaterialTheme.colorScheme.surface.toArgb()

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            LineChart(ctx).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(true)
                setBackgroundColor(surfaceColor)
                legend.form = Legend.LegendForm.LINE

                axisRight.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = onSurface
                }

                axisLeft.apply {
                    setDrawGridLines(true)
                    textColor = onSurface
                    axisMinimum = 0f
                }

                setNoDataText("No data")
            }
        },
        update = { chart ->
            val entries = dataPoints.map { Entry(it.first, it.second) }
            val set = LineDataSet(entries, label).apply {
                color = primaryColor
                lineWidth = 2.5f
                setDrawCircles(true)
                circleRadius = 4f
                setCircleColor(primaryColor)
                valueTextColor = onSurface
                valueTextSize = 10f
                mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                setDrawFilled(true)
                fillColor = primaryColor
                fillAlpha = 70
                setDrawValues(false)
            }

            val data = LineData(set)
            chart.data = data

            // x labels
            if (xLabels.isNotEmpty()) {
                chart.xAxis.valueFormatter = IndexAxisValueFormatter(xLabels)
                chart.xAxis.labelRotationAngle = -40f
                chart.xAxis.labelCount = xLabels.size
            }

            chart.invalidate()
            chart.animateX(600)
            chart.notifyDataSetChanged()
        }
    )
}

/* ---------------- Firestore / ID 解析 逻辑 ---------------- */

/**
 * 从 order id 中解析出日期（三位：year, month, day）
 * 支持 id 内含 yyMMdd 或 yyyyMMdd 的子串（例如 ORD-250921xxx 或 ORD-20250921xxx）
 * 返回 Triple(year, month, day) 或 null
 */
private fun parseDateFromId(id: String): Triple<Int, Int, Int>? {
    // 先找 8 位 (yyyyMMdd)
    val re8 = Regex("""\d{8}""")
    val match8 = re8.find(id)
    if (match8 != null) {
        val s = match8.value
        val yyyy = s.substring(0, 4).toIntOrNull() ?: return null
        val mm = s.substring(4, 6).toIntOrNull() ?: return null
        val dd = s.substring(6, 8).toIntOrNull() ?: return null
        return Triple(yyyy, mm, dd)
    }
    // 再找 6 位 (yyMMdd)
    val re6 = Regex("""\d{6}""")
    val match6 = re6.find(id)
    if (match6 != null) {
        val s = match6.value
        val yy = s.substring(0, 2).toIntOrNull() ?: return null
        val mm = s.substring(2, 4).toIntOrNull() ?: return null
        val dd = s.substring(4, 6).toIntOrNull() ?: return null
        val yyyy = 2000 + yy // 假设 2000 年以后
        return Triple(yyyy, mm, dd)
    }
    return null
}

suspend fun getOrderCountForYear(db: FirebaseFirestore, year: Int): Int {
    val snap = db.collection("orders").get().await()
    return snap.documents.count { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed?.first == year
    }
}

suspend fun getOrderCountForMonth(db: FirebaseFirestore, month: Int): Int {
    // 统计当年指定月份（month:1..12）
    val snap = db.collection("orders").get().await()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return snap.documents.count { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed != null && parsed.first == currentYear && parsed.second == month
    }
}

suspend fun getSalesTotalForYear(db: FirebaseFirestore, year: Int): Double {
    val snap = db.collection("orders").get().await()
    return snap.documents.filter { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed?.first == year
    }.sumOf { doc -> (doc.getDouble("cost") ?: doc.getLong("cost")?.toDouble()) ?: 0.0 }
}

suspend fun getSalesTotalForMonth(db: FirebaseFirestore, month: Int): Double {
    val snap = db.collection("orders").get().await()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    return snap.documents.filter { doc ->
        val idField = doc.getString("id")?.takeIf { it.isNotBlank() } ?: doc.id
        val parsed = parseDateFromId(idField)
        parsed != null && parsed.first == currentYear && parsed.second == month
    }.sumOf { doc -> (doc.getDouble("cost") ?: doc.getLong("cost")?.toDouble()) ?: 0.0 }
}

/* ---------------- 辅助 ---------------- */
private fun monthNumberToLabel(month: Int): String {
    val cal = Calendar.getInstance()
    cal.set(Calendar.MONTH, month - 1)
    val sdf = SimpleDateFormat("MMM", Locale.getDefault())
    return sdf.format(cal.time)
}
