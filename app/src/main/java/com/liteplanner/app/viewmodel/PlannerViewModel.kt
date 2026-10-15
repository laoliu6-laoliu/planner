package com.liteplanner.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liteplanner.app.data.*
import com.liteplanner.app.notification.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).plannerDao()
    private val appContext = application.applicationContext

    val events: StateFlow<List<ScheduleEvent>> = dao.getAllEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TodoTask>> = dao.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<HabitItem>> = dao.getAllHabits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pomodoroSessions: StateFlow<List<PomodoroSession>> = dao.getAllPomodoroSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 当前选中的日期 (默认今天)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // 番茄钟状态
    private val _pomodoroMode = MutableStateFlow("专注") // 专注 / 短休息 / 长休息
    val pomodoroMode: StateFlow<String> = _pomodoroMode.asStateFlow()

    private val _totalSeconds = MutableStateFlow(25 * 60)
    val totalSeconds: StateFlow<Int> = _totalSeconds.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(25 * 60)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _boundTaskTitle = MutableStateFlow("深度工作专注")
    val boundTaskTitle: StateFlow<String> = _boundTaskTitle.asStateFlow()

    private var timerJob: Job? = null

    init {
        NotificationHelper.ensureChannel(appContext)
        seedInitialDataIfNeeded()
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            if (events.first().isEmpty() && tasks.first().isEmpty() && habits.first().isEmpty()) {
                val today = LocalDate.now().toString()
                dao.insertEvent(
                    ScheduleEvent(
                        title = "晨间计划与今日复盘",
                        dateIso = today,
                        startTime = "09:00",
                        endTime = "09:30",
                        category = "工作",
                        note = "梳理今日四象限重点任务",
                        notifyEnabled = true
                    )
                )
                dao.insertEvent(
                    ScheduleEvent(
                        title = "项目核心模块开发",
                        dateIso = today,
                        startTime = "14:00",
                        endTime = "16:00",
                        category = "学习",
                        note = "保持专注，开启2个番茄钟",
                        notifyEnabled = true
                    )
                )
                dao.insertTask(TodoTask(title = "完成本周核心方案初稿", quadrant = 1, dueDateIso = today, dueTime = "11:30"))
                dao.insertTask(TodoTask(title = "阅读专业书籍 30 分钟", quadrant = 2, dueDateIso = today, dueTime = "20:00"))
                dao.insertTask(TodoTask(title = "回复日常群组消息与邮件", quadrant = 3, dueDateIso = today, dueTime = "17:00"))
                dao.insertTask(TodoTask(title = "整理手机相册与桌面归档", quadrant = 4, dueDateIso = today, dueTime = "21:30"))

                dao.insertHabit(HabitItem(name = "早起喝一杯温水", icon = "💧", reminderTime = "07:30", checkedDatesCsv = today))
                dao.insertHabit(HabitItem(name = "每日运动 30 分钟", icon = "🏃", reminderTime = "18:30", checkedDatesCsv = ""))
                dao.insertHabit(HabitItem(name = "睡前阅读与复盘", icon = "📖", reminderTime = "22:00", checkedDatesCsv = ""))
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addEvent(title: String, dateIso: String, startTime: String, endTime: String, category: String, note: String, notify: Boolean) {
        viewModelScope.launch {
            val id = dao.insertEvent(
                ScheduleEvent(
                    title = title,
                    dateIso = dateIso,
                    startTime = startTime,
                    endTime = endTime,
                    category = category,
                    note = note,
                    notifyEnabled = notify
                )
            )
            if (notify) {
                NotificationHelper.scheduleMessageReminder(
                    context = appContext,
                    uniqueId = (10000 + id).toInt(),
                    dateIso = dateIso,
                    timeHm = startTime,
                    title = "日程提醒：$title",
                    message = "$startTime - $endTime ($category) $note"
                )
            }
        }
    }

    fun deleteEvent(event: ScheduleEvent) {
        viewModelScope.launch { dao.deleteEvent(event) }
    }

    fun addTask(title: String, quadrant: Int, dueDateIso: String, dueTime: String, notify: Boolean) {
        viewModelScope.launch {
            val id = dao.insertTask(
                TodoTask(
                    title = title,
                    quadrant = quadrant,
                    dueDateIso = dueDateIso,
                    dueTime = dueTime,
                    notifyEnabled = notify
                )
            )
            if (notify) {
                NotificationHelper.scheduleMessageReminder(
                    context = appContext,
                    uniqueId = (20000 + id).toInt(),
                    dateIso = dueDateIso,
                    timeHm = dueTime,
                    title = "待办提醒：$title",
                    message = "截止时间 $dueTime · 请及时处理"
                )
            }
        }
    }

    fun toggleTask(task: TodoTask) {
        viewModelScope.launch {
            dao.updateTask(task.copy(completed = !task.completed))
        }
    }

    fun moveTaskQuadrant(task: TodoTask, newQuadrant: Int) {
        viewModelScope.launch {
            dao.updateTask(task.copy(quadrant = newQuadrant))
        }
    }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch { dao.deleteTask(task) }
    }

    fun addHabit(name: String, icon: String, reminderTime: String, notify: Boolean) {
        viewModelScope.launch {
            val id = dao.insertHabit(
                HabitItem(name = name, icon = icon, reminderTime = reminderTime, notifyEnabled = notify)
            )
            if (notify) {
                NotificationHelper.scheduleMessageReminder(
                    context = appContext,
                    uniqueId = (30000 + id).toInt(),
                    dateIso = LocalDate.now().toString(),
                    timeHm = reminderTime,
                    title = "习惯打卡提醒：$icon $name",
                    message = "今天别忘了完成「$name」打卡哦！"
                )
            }
        }
    }

    fun toggleHabitDate(habit: HabitItem, dateIso: String) {
        viewModelScope.launch {
            val current = habit.checkedSet().toMutableSet()
            if (current.contains(dateIso)) {
                current.remove(dateIso)
            } else {
                current.add(dateIso)
            }
            dao.updateHabit(habit.copy(checkedDatesCsv = current.sorted().joinToString(",")))
        }
    }

    fun deleteHabit(habit: HabitItem) {
        viewModelScope.launch { dao.deleteHabit(habit) }
    }

    // 番茄钟控制
    fun setPomodoroMode(mode: String, minutes: Int) {
        pausePomodoro()
        _pomodoroMode.value = mode
        _totalSeconds.value = minutes * 60
        _remainingSeconds.value = minutes * 60
    }

    fun bindTaskToPomodoro(title: String) {
        _boundTaskTitle.value = title
    }

    fun togglePomodoro() {
        if (_isRunning.value) {
            pausePomodoro()
        } else {
            startPomodoro()
        }
    }

    private fun startPomodoro() {
        if (_isRunning.value) return
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_remainingSeconds.value > 0 && _isRunning.value) {
                delay(1000L)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value == 0 && _isRunning.value) {
                _isRunning.value = false
                val mins = (_totalSeconds.value / 60).coerceAtLeast(1)
                if (_pomodoroMode.value == "专注") {
                    dao.insertPomodoroSession(
                        PomodoroSession(
                            taskTitle = _boundTaskTitle.value,
                            durationMinutes = mins,
                            dateIso = LocalDate.now().toString(),
                            completedAtTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
                        )
                    )
                }
                NotificationHelper.showInstantNotification(
                    context = appContext,
                    notificationId = 99001,
                    title = "${_pomodoroMode.value}计时结束",
                    message = "已完成 $mins 分钟「${_boundTaskTitle.value}」，休息一下吧！"
                )
                _remainingSeconds.value = _totalSeconds.value
            }
        }
    }

    private fun pausePomodoro() {
        _isRunning.value = false
        timerJob?.cancel()
    }

    fun resetPomodoro() {
        pausePomodoro()
        _remainingSeconds.value = _totalSeconds.value
    }
}
