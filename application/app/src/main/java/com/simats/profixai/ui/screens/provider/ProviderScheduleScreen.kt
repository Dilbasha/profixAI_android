package com.simats.profixai.ui.screens.provider

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderScheduleScreen(navController: NavController, providerId: Int) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var availability by remember { mutableStateOf<Map<LocalDate, ProviderAvailability>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    
    // Selected day editing state
    var selectedStatus by remember { mutableStateOf("available") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("17:00") }
    
    // Copy dialog state
    var showCopyDialog by remember { mutableStateOf(false) }
    var selectedCopyDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    
    val scope = rememberCoroutineScope()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    // Load availability for current month
    fun loadAvailability() {
        scope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.apiService.getProviderAvailability(
                    GetAvailabilityRequest(
                        provider_id = providerId,
                        year = currentMonth.year,
                        month = currentMonth.monthValue
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    availability = response.body()?.availability?.associate { avail ->
                        LocalDate.parse(avail.date) to avail
                    } ?: emptyMap()
                }
            } catch (e: Exception) {
                message = "Error loading schedule"
            } finally {
                isLoading = false
            }
        }
    }
    
    // Load when month changes
    LaunchedEffect(currentMonth) {
        loadAvailability()
    }
    
    // Update selected day state when date is selected
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            val avail = availability[date]
            if (avail != null) {
                selectedStatus = avail.status
                startTime = avail.start_time
                endTime = avail.end_time
            } else {
                selectedStatus = "available"
                startTime = "09:00"
                endTime = "17:00"
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Schedule") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A1A2E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF1A1A2E))
        ) {
            // Month Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, "Previous Month", tint = Color.White)
                }
                Text(
                    text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, "Next Month", tint = Color.White)
                }
            }
            
            // Calendar Grid
            CalendarGrid(
                yearMonth = currentMonth,
                selectedDate = selectedDate,
                availability = availability,
                onDateSelected = { date -> selectedDate = date }
            )
            
            // Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = Green500, label = "Available")
                Spacer(modifier = Modifier.width(24.dp))
                LegendItem(color = Gray500, label = "Unavailable")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Edit Panel
            if (selectedDate != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A3E))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Edit: ${selectedDate?.format(DateTimeFormatter.ofPattern("MMMM d"))}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Status Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1A1A2E))
                                .padding(4.dp)
                        ) {
                            StatusToggleButton(
                                text = "Available",
                                selected = selectedStatus == "available",
                                modifier = Modifier.weight(1f)
                            ) { selectedStatus = "available" }
                            
                            StatusToggleButton(
                                text = "Unavailable",
                                selected = selectedStatus == "unavailable",
                                modifier = Modifier.weight(1f)
                            ) { selectedStatus = "unavailable" }
                        }
                        
                        // Working Hours (only show if available or partial)
                        if (selectedStatus != "unavailable") {
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Working Hours",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Gray400
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TimePickerButton(
                                    time = startTime,
                                    modifier = Modifier.weight(1f)
                                ) { newTime -> startTime = newTime }
                                
                                TimePickerButton(
                                    time = endTime,
                                    modifier = Modifier.weight(1f)
                                ) { newTime -> endTime = newTime }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Copy Schedule Button with dashed border
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        color = Gray500,
                                        cornerRadius = CornerRadius(12.dp.toPx()),
                                        style = Stroke(
                                            width = 1.5.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(
                                                floatArrayOf(10f, 10f), 0f
                                            )
                                        )
                                    )
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCopyDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Copy schedule to other days", color = Color.White, fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Message
                        if (message != null) {
                            Text(
                                text = message!!,
                                color = if (message!!.contains("success", true)) Green500 else Red500,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        // Update Button
                        Button(
                            onClick = {
                                scope.launch {
                                    isSaving = true
                                    message = null
                                    try {
                                        val response = RetrofitClient.apiService.updateProviderAvailability(
                                            UpdateAvailabilityRequest(
                                                provider_id = providerId,
                                                date = selectedDate!!.format(dateFormatter),
                                                status = selectedStatus,
                                                start_time = startTime,
                                                end_time = endTime
                                            )
                                        )
                                        if (response.isSuccessful && response.body()?.success == true) {
                                            message = "Availability updated!"
                                            loadAvailability()
                                        } else {
                                            message = response.body()?.message ?: "Update failed"
                                        }
                                    } catch (e: Exception) {
                                        message = "Error: ${e.message}"
                                    } finally {
                                        isSaving = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Update Availability", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                // Prompt to select a date
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.TouchApp,
                            null,
                            modifier = Modifier.size(48.dp),
                            tint = Gray500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Select a date to set availability",
                            color = Gray500,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
    
    // Copy Schedule Dialog
    if (showCopyDialog && selectedDate != null) {
        CopyScheduleDialog(
            currentMonth = currentMonth,
            selectedDate = selectedDate!!,
            availability = availability,
            onDismiss = { showCopyDialog = false },
            onConfirm = { targetDates ->
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.copyAvailability(
                            CopyAvailabilityRequest(
                                provider_id = providerId,
                                source_date = selectedDate!!.format(dateFormatter),
                                target_dates = targetDates.map { it.format(dateFormatter) }
                            )
                        )
                        if (response.isSuccessful && response.body()?.success == true) {
                            message = "Schedule copied successfully!"
                            loadAvailability()
                        } else {
                            message = response.body()?.message ?: "Copy failed"
                        }
                    } catch (e: Exception) {
                        message = "Error: ${e.message}"
                    }
                    showCopyDialog = false
                }
            }
        )
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    availability: Map<LocalDate, ProviderAvailability>,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysOfWeek = listOf("Su", "M", "T", "W", "T", "F", "Sa")
    val firstDayOfMonth = yearMonth.atDay(1)
    val lastDayOfMonth = yearMonth.atEndOfMonth()
    val startOffset = firstDayOfMonth.dayOfWeek.value % 7
    
    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        // Day headers
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    color = Gray500
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Calendar days
        var currentDay = 1
        val totalDays = lastDayOfMonth.dayOfMonth
        val today = LocalDate.now()
        
        for (week in 0..5) {
            if (currentDay > totalDays) break
            
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    if (week == 0 && dayOfWeek < startOffset) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else if (currentDay <= totalDays) {
                        val date = yearMonth.atDay(currentDay)
                        val avail = availability[date]
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        
                        CalendarDay(
                            day = currentDay,
                            isSelected = isSelected,
                            isToday = isToday,
                            status = avail?.status,
                            modifier = Modifier.weight(1f),
                            onClick = { onDateSelected(date) }
                        )
                        currentDay++
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun CalendarDay(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    status: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> Blue600
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> Color.White
        else -> Color.White
    }
    
    val indicatorColor = when (status) {
        "available" -> Green500
        "unavailable" -> Gray500
        else -> null
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) 
                    Modifier.border(1.dp, Blue600, CircleShape) 
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                color = textColor
            )
            if (indicatorColor != null && !isSelected) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 12.sp, color = Gray400)
    }
}

@Composable
fun StatusToggleButton(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Blue600 else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Gray400,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun TimePickerButton(
    time: String,
    modifier: Modifier = Modifier,
    onTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Format display time
    val displayTime = try {
        val parts = time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1]
        val amPm = if (hour >= 12) "PM" else "AM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        String.format("%02d:%s %s", displayHour, minute, amPm)
    } catch (e: Exception) {
        time
    }
    
    OutlinedButton(
        onClick = { showTimePicker = true },
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray600)
    ) {
        Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp), tint = Gray400)
        Spacer(modifier = Modifier.width(8.dp))
        Text(displayTime, fontSize = 14.sp)
    }
    
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onConfirm = { newTime ->
                onTimeSelected(newTime)
                showTimePicker = false
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val parts = initialTime.split(":")
    var hour by remember { mutableStateOf(parts.getOrNull(0)?.toIntOrNull() ?: 9) }
    var minute by remember { mutableStateOf(parts.getOrNull(1)?.toIntOrNull() ?: 0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A3E),
        title = { Text("Select Time", color = Color.White) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { if (hour < 23) hour++ }) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                    }
                    Text(
                        String.format("%02d", hour),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = { if (hour > 0) hour-- }) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                    }
                }
                
                Text(":", fontSize = 32.sp, color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
                
                // Minute picker
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { minute = (minute + 15) % 60 }) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = Color.White)
                    }
                    Text(
                        String.format("%02d", minute),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = { minute = if (minute >= 15) minute - 15 else 45 }) {
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.White)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(String.format("%02d:%02d", hour, minute)) },
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray400)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyScheduleDialog(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    availability: Map<LocalDate, ProviderAvailability>,
    onDismiss: () -> Unit,
    onConfirm: (List<LocalDate>) -> Unit
) {
    var selectedDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF2A2A3E),
        title = { Text("Copy Schedule To", color = Color.White) },
        text = {
            Column {
                Text(
                    "Select days to copy the schedule from ${selectedDate.format(DateTimeFormatter.ofPattern("MMM d"))}",
                    color = Gray400,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quick select options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            // Select all weekdays in month
                            val weekdays = mutableListOf<LocalDate>()
                            var date = currentMonth.atDay(1)
                            while (date.month == currentMonth.month) {
                                if (date.dayOfWeek.value in 1..5 && date != selectedDate) {
                                    weekdays.add(date)
                                }
                                date = date.plusDays(1)
                            }
                            selectedDates = weekdays.toSet()
                        },
                        label = { Text("Weekdays", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1A1A2E),
                            labelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            // Select all weekends in month
                            val weekends = mutableListOf<LocalDate>()
                            var date = currentMonth.atDay(1)
                            while (date.month == currentMonth.month) {
                                if (date.dayOfWeek.value in 6..7 && date != selectedDate) {
                                    weekends.add(date)
                                }
                                date = date.plusDays(1)
                            }
                            selectedDates = weekends.toSet()
                        },
                        label = { Text("Weekends", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1A1A2E),
                            labelColor = Color.White
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mini calendar for selection
                Text("Selected: ${selectedDates.size} days", color = Gray400, fontSize = 12.sp)
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedDates.toList()) },
                enabled = selectedDates.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Text("Copy to ${selectedDates.size} days")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray400)
            }
        }
    )
}
