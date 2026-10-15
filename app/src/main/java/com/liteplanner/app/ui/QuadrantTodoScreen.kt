package com.liteplanner.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.liteplanner.app.data.TodoTask
import com.liteplanner.app.viewmodel.PlannerViewModel
import java.time.LocalDate

data class QuadrantMeta(
    val id: Int,
    val title: String,
    val subtitle: String,
    val badgeColor: Color,
    val cardBg: Color
)

private val quadrantList = listOf(
    QuadrantMeta(1, "第一象限：重要且紧急", "立即去做 · 核心交付与危机", Color(0xFFE11D48), Color(0xFFFFF1F2)),
    QuadrantMeta(2, "第二象限：重要不紧急", "有计划做 · 长期目标与成长", Color(0xFF4F46E5), Color(0xFFEEF2FF)),
    QuadrantMeta(3, "第三象限：紧急不重要", "尽快处理 · 琐事与临时配合", Color(0xFFD97706), Color(0xFFFFFBEB)),
    QuadrantMeta(4, "第四象限：不重要不紧急", "稍后或精简 · 休闲与低优事项", Color(0xFF0D9488), Color(0xFFF0FDFA))
)

@Composable
fun QuadrantTodoScreen(viewModel: PlannerViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "新增待办") },
                text = { Text("新增四象限待办") },
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
            items(quadrantList, key = { it.id }) { q ->
                val qTasks = tasks.filter { it.quadrant == q.id }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = q.cardBg)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = q.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = q.badgeColor)
                                Text(text = q.subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                            Surface(color = q.badgeColor, shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    text = "${qTasks.count { !it.completed }} 待办",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (qTasks.isEmpty()) {
                            Text("暂无待办事项", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        } else {
                            qTasks.forEach { task ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = task.completed,
                                        onCheckedChange = { viewModel.toggleTask(task) }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = task.title,
                                            fontSize = 14.sp,
                                            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (task.completed) Color(0xFF94A3B8) else Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "截止：${task.dueDateIso} ${task.dueTime}" + if (task.notifyEnabled) " · 消息提醒" else "",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteTask(task) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "删除", tint = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        var title by remember { mutableStateOf("") }
        var quadrant by remember { mutableStateOf(1) }
        var dueDate by remember { mutableStateOf(LocalDate.now().toString()) }
        var dueTime by remember { mutableStateOf("18:00") }
        var notify by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("新建四象限待办") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("待办内容") })
                    Text("选择优先级象限：", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    quadrantList.forEach { meta ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = quadrant == meta.id, onClick = { quadrant = meta.id })
                            Text(meta.title, fontSize = 13.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("日期") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = dueTime, onValueChange = { dueTime = it }, label = { Text("时间") }, modifier = Modifier.weight(1f))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = notify, onCheckedChange = { notify = it })
                        Text("发送到期系统消息通知", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (title.isNotBlank()) {
                        viewModel.addTask(title.trim(), quadrant, dueDate.trim(), dueTime.trim(), notify)
                        showDialog = false
                    }
                }) { Text("添加") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("取消") }
            }
        )
    }
}
