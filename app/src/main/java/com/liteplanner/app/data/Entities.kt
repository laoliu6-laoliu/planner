package com.liteplanner.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedule_events")
data class ScheduleEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val dateIso: String,       // YYYY-MM-DD
    val startTime: String,     // HH:mm
    val endTime: String,       // HH:mm
    val category: String,      // 工作 / 学习 / 生活 / 健康
    val note: String = "",
    val notifyEnabled: Boolean = true
)

/**
 * 四象限优先级：
 * 1 = 重要且紧急 (Q1)
 * 2 = 重要不紧急 (Q2)
 * 3 = 紧急不重要 (Q3)
 * 4 = 不重要不紧急 (Q4)
 */
@Entity(tableName = "todo_tasks")
data class TodoTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val quadrant: Int,
    val dueDateIso: String,    // YYYY-MM-DD
    val dueTime: String,       // HH:mm
    val completed: Boolean = false,
    val notifyEnabled: Boolean = true
)

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,
    val reminderTime: String,  // HH:mm
    val notifyEnabled: Boolean = true,
    // 以逗号分隔的已打卡日期字符串，例如 "2026-10-13,2026-10-14,2026-10-15"
    val checkedDatesCsv: String = ""
) {
    fun checkedSet(): Set<String> =
        checkedDatesCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}

@Entity(tableName = "pomodoro_sessions")
data class PomodoroSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskTitle: String,
    val durationMinutes: Int,
    val dateIso: String,
    val completedAtTime: String
)
