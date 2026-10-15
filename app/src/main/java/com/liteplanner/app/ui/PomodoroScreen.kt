package com.liteplanner.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

@Composable
fun PomodoroScreen(viewModel: PlannerViewModel) {
    val mode by viewModel.pomodoroMode.collectAsState()
    val remaining by viewModel.remainingSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val boundTask by viewModel.boundTaskTitle.collectAsState()
    val sessions by viewModel.pomodoroSessions.collectAsState()

    val minutes = remaining / 60
    val seconds = remaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    val totalFocusMins = sessions.sumOf { it.durationMinutes }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 模式切换
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("专注" to 25, "短休息" to 5, "长休息" to 15).forEach { (m, mins) ->
                val selected = mode == m
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.setPomodoroMode(m, mins) },
                    label = { Text("$m (${mins}分)") }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 计时大圆盘
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = mode, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                Text(
                    text = timeFormatted,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B)
                )
                Text(text = boundTask, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { viewModel.togglePomodoro() }) {
                Text(if (isRunning) "暂停计时" else "开始专注")
            }
            OutlinedButton(onClick = { viewModel.resetPomodoro() }) {
                Text("重置")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "累计完成 ${sessions.size} 个番茄钟 · 共 $totalFocusMins 分钟",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(sessions, key = { it.id }) { s ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🍅 ${s.taskTitle}", fontSize = 13.sp)
                            Text("${s.dateIso} ${s.completedAtTime} (+${s.durationMinutes}分)", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    }
                }
            }
        }
    }
}
