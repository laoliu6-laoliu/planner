package com.liteplanner.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liteplanner.app.data.ScheduleEvent
import com.liteplanner.app.viewmodel.PlannerViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

@Composable
fun CalendarScreen(viewModel: PlannerViewModel) {
    val events by viewModel.events.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    var viewMode by remember { mutableStateOf("日") } // 日 / 周 / 月
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "新增日程") },
                text = { Text("新建日程") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // 顶部日期切换与日/周/月视图切换器
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        val next = when (viewMode) {
                            "日" -> selectedDate.minusDays(1)
                            "周" -> selectedDate.minusWeeks(1)
                            else -> selectedDate.minusMonths(1)
                        }
                        viewModel.selectDate(next)
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "上一页")
                    }
                    Text(
                        text = "${selectedDate.year}年${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        val next = when (viewMode) {
                            "日" -> selectedDate.plusDays(1)
                            "周" -> selectedDate.plusWeeks(1)
                            else -> selectedDate.plusMonths(1)
                        }
                        viewModel.selectDate(next)
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "下一页")
                    }
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFE2E8F0))
                        .padding(2.dp)
                ) {
                    listOf("日", "周", "月").forEach { mode ->
                        val active = viewMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent)
                                .clickable { viewMode = mode }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = mode,
                                color = if (active) Color.White else Color(0xFF475569),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (viewMode) {
                "日" -> DayTimelineView(
                    selectedDate = selectedDate,
                    events = events.filter { it.dateIso == selectedDate.toString() },
                    onDelete = { viewModel.deleteEvent(it) }
                )
                "周" -> WeekCalendarView(
                    selectedDate = selectedDate,
                    events = events,
                    onSelectDate = { viewModel.selectDate(it) },
                    onDelete = { viewModel.deleteEvent(it) }
                )
                "月" -> MonthCalendarView(
                    selectedDate = selectedDate,
                    events = events,
                    onSelectDate = { viewModel.selectDate(it) },
                    onDelete = { viewModel.deleteEvent(it) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEventDialog(
            initialDate = selectedDate.toString(),
            onDismiss = { showAddDialog = false },
            onConfirm = { title, dateIso, start, end, category, note, notify ->
                viewModel.addEvent(title, dateIso, start, end, category, note, notify)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun DayTimelineView(
    selectedDate: LocalDate,
    events: List<ScheduleEvent>,
    onDelete: (ScheduleEvent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "${selectedDate} · 共 ${events.size} 项日程安排",
            fontSize = 13.sp,
            color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("今日暂无日程，点击右下角新建日程", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(events, key = { it.id }) { ev ->
                    EventCard(ev, onDelete)
                }
            }
        }
    }
}

@Composable
private fun WeekCalendarView(
    selectedDate: LocalDate,
    events: List<ScheduleEvent>,
    onSelectDate: (LocalDate) -> Unit,
    onDelete: (ScheduleEvent) -> Unit
) {
    val startOfWeek = selectedDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0..6).map { startOfWeek.plusDays(it.toLong()) }
    val dayNames = listOf("一", "二", "三", "四", "五", "六", "日")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekDays.forEachIndexed { idx, day ->
                val isSelected = day == selectedDate
                val count = events.count { it.dateIso == day.toString() }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                        .clickable { onSelectDate(day) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "周${dayNames[idx]}",
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else Color(0xFF64748B)
                    )
                    Text(
                        text = "${day.dayOfMonth}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF0F172A)
                    )
                    Text(
                        text = if (count > 0) "${count}项" else "-",
                        fontSize = 10.sp,
                        color = if (isSelected) Color(0xFFE0E7FF) else Color(0xFF94A3B8)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        DayTimelineView(
            selectedDate = selectedDate,
            events = events.filter { it.dateIso == selectedDate.toString() },
            onDelete = onDelete
        )
    }
}

@Composable
private fun MonthCalendarView(
    selectedDate: LocalDate,
    events: List<ScheduleEvent>,
    onSelectDate: (LocalDate) -> Unit,
    onDelete: (ScheduleEvent) -> Unit
) {
    val yearMonth = YearMonth.from(selectedDate)
    val firstDay = yearMonth.atDay(1)
    val leadingEmpty = (firstDay.dayOfWeek.value - 1).coerceAtLeast(0)
    val daysInMonth = yearMonth.lengthOfMonth()
    val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7

    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        for (row in 0 until (totalCells / 7)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val cellIndex = row * 7 + col
                    val dayNum = cellIndex - leadingEmpty + 1
                    if (dayNum in 1..daysInMonth) {
                        val date = yearMonth.atDay(dayNum)
                        val isSelected = date == selectedDate
                        val hasEvents = events.any { it.dateIso == date.toString() }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.White)
                                .clickable { onSelectDate(date) }
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$dayNum",
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF0F172A)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            !hasEvents -> Color.Transparent
                                            isSelected -> Color.White
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    )
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        DayTimelineView(
            selectedDate = selectedDate,
            events = events.filter { it.dateIso == selectedDate.toString() },
            onDelete = onDelete
        )
    }
}

@Composable
private fun EventCard(event: ScheduleEvent, onDelete: (ScheduleEvent) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${event.startTime} - ${event.endTime}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = event.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (event.notifyEnabled) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = "消息提醒已开启",
                            tint = Color(0xFF0D9488),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = event.title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                if (event.note.isNotBlank()) {
                    Text(text = event.note, fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
            IconButton(onClick = { onDelete(event) }) {
                Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFF94A3B8))
            }
        }
    }
}

@Composable
private fun AddEventDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var dateIso by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var category by remember { mutableStateOf("工作") }
    var note by remember { mutableStateOf("") }
    var notify by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新建日程安排") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("日程标题") })
                OutlinedTextField(value = dateIso, onValueChange = { dateIso = it }, label = { Text("日期 (YYYY-MM-DD)") })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("开始 (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("结束 (HH:mm)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("分类 (工作/学习/生活/健康)") })
                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("备注说明") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = notify, onCheckedChange = { notify = it })
                    Text("到点发送系统消息通知提醒", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(title.trim(), dateIso.trim(), startTime.trim(), endTime.trim(), category.trim(), note.trim(), notify)
                    }
                }
            ) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}
