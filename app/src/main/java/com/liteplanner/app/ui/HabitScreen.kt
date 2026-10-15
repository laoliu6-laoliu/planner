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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liteplanner.app.viewmodel.PlannerViewModel
import java.time.LocalDate

@Composable
fun HabitScreen(viewModel: PlannerViewModel) {
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val last7Days = (6 downTo 0).map { today.minusDays(it.toLong()) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "新增习惯") },
                text = { Text("添加习惯") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                val checkedSet = habit.checkedSet()
                val checkedToday = checkedSet.contains(today.toString())

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = habit.icon, fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(text = habit.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(
                                        text = "累计打卡 ${checkedSet.size} 天 · 每日提醒 ${habit.reminderTime}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { viewModel.toggleHabitDate(habit, today.toString()) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (checkedToday) Color(0xFF0D9488) else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(if (checkedToday) "今日已打卡" else "打卡")
                                }
                                IconButton(onClick = { viewModel.deleteHabit(habit) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFF94A3B8))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 最近 7 天打卡矩阵
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            last7Days.forEach { d ->
                                val iso = d.toString()
                                val done = checkedSet.contains(iso)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { viewModel.toggleHabitDate(habit, iso) }
                                ) {
                                    Text(text = "${d.monthValue}/${d.dayOfMonth}", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .background(if (done) Color(0xFF0D9488) else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (done) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = "已打卡",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
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
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var icon by remember { mutableStateOf("✨") }
        var reminderTime by remember { mutableStateOf("20:00") }
        var notify by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("新建习惯打卡") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("习惯名称 (例如：背单词、早睡)") })
                    OutlinedTextField(value = icon, onValueChange = { icon = it }, label = { Text("图标 Emoji") })
                    OutlinedTextField(value = reminderTime, onValueChange = { reminderTime = it }, label = { Text("每日消息提醒时间 (HH:mm)") })
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = notify, onCheckedChange = { notify = it })
                        Text("开启每日通知栏消息提醒", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.addHabit(name.trim(), icon.trim().ifEmpty { "✨" }, reminderTime.trim(), notify)
                        showAddDialog = false
                    }
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}
