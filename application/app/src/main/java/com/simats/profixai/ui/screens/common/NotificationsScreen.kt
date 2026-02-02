package com.simats.profixai.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    navController: NavController,
    userId: Int = 0,
    providerId: Int = 0,
    isProvider: Boolean = false
) {
    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    
    // Load notifications from API
    LaunchedEffect(userId, providerId) {
        try {
            val request = if (isProvider) {
                GetNotificationsRequest(provider_id = providerId)
            } else {
                GetNotificationsRequest(user_id = userId)
            }
            val response = RetrofitClient.apiService.getNotifications(request)
            if (response.isSuccessful && response.body()?.success == true) {
                notifications = response.body()?.notifications ?: emptyList()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    // Group notifications by date
    val todayNotifications = notifications.filter { isToday(it.created_at) }
    val yesterdayNotifications = notifications.filter { isYesterday(it.created_at) }
    val thisWeekNotifications = notifications.filter { 
        !isToday(it.created_at) && !isYesterday(it.created_at) && isThisWeek(it.created_at)
    }
    val olderNotifications = notifications.filter {
        !isToday(it.created_at) && !isYesterday(it.created_at) && !isThisWeek(it.created_at)
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isProvider) Color(0xFF1565C0) else Color(0xFF00897B),
                            if (isProvider) Color(0xFF0D47A1) else Color(0xFF00695C)
                        )
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    "Notifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = {
                    scope.launch {
                        try {
                            val request = if (isProvider) {
                                MarkNotificationReadRequest(provider_id = providerId, mark_all = true)
                            } else {
                                MarkNotificationReadRequest(user_id = userId, mark_all = true)
                            }
                            RetrofitClient.apiService.markNotificationRead(request)
                            // Update UI
                            notifications = notifications.map { it.copy(is_read = true) }
                        } catch (e: Exception) { }
                    }
                }) {
                    Icon(Icons.Default.DoneAll, "Mark all read", tint = Color.White)
                }
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (notifications.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = 100.dp), Alignment.TopCenter) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, null, Modifier.size(80.dp), Color.White.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No notifications yet", color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Today
                    if (todayNotifications.isNotEmpty()) {
                        item {
                            Text("Today", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(todayNotifications) { notification ->
                            NotificationCard(notification) {
                                scope.launch {
                                    try {
                                        RetrofitClient.apiService.markNotificationRead(
                                            MarkNotificationReadRequest(notification_id = notification.id)
                                        )
                                        notifications = notifications.map {
                                            if (it.id == notification.id) it.copy(is_read = true) else it
                                        }
                                    } catch (e: Exception) { }
                                }
                            }
                        }
                    }
                    
                    // Yesterday
                    if (yesterdayNotifications.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Yesterday", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gray300)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(yesterdayNotifications) { notification ->
                            NotificationCard(notification) { }
                        }
                    }
                    
                    // This Week
                    if (thisWeekNotifications.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("This week", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gray300)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(thisWeekNotifications) { notification ->
                            NotificationCard(notification) { }
                        }
                    }
                    
                    // Older
                    if (olderNotifications.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Earlier", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Gray300)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(olderNotifications) { notification ->
                            NotificationCard(notification) { }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(notification: Notification, onClick: () -> Unit) {
    val (icon, iconColor, iconBgColor) = getNotificationIcon(notification.type)
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A3F50))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp)).background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = iconColor)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(notification.title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(notification.message, fontSize = 13.sp, color = Gray400)
                Spacer(modifier = Modifier.height(4.dp))
                Text(formatTimeAgo(notification.created_at), fontSize = 11.sp, color = Gray500)
            }
            
            if (!notification.is_read) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Blue600))
            }
        }
    }
}

fun getNotificationIcon(type: String): Triple<ImageVector, Color, Color> {
    return when (type) {
        "booking_created", "booking_submitted" -> Triple(Icons.Default.CalendarMonth, Color.White, Blue600)
        "booking_accepted" -> Triple(Icons.Default.CheckCircle, Color.White, Green500)
        "booking_started" -> Triple(Icons.Default.DirectionsCar, Color.White, Orange500)
        "booking_completed", "job_completed" -> Triple(Icons.Default.Done, Green500, Green500.copy(alpha = 0.2f))
        "booking_cancelled" -> Triple(Icons.Default.Cancel, Color.White, Red500)
        "rating_request" -> Triple(Icons.Default.Star, Amber500, Amber500.copy(alpha = 0.2f))
        "payment_due" -> Triple(Icons.Default.Payment, Color.White, Green500)
        else -> Triple(Icons.Default.Notifications, Color.White, Blue600)
    }
}

fun formatTimeAgo(dateString: String?): String {
    if (dateString == null) return ""
    try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString) ?: return dateString
        val now = Date()
        val diff = now.time - date.time
        
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            days < 7 -> "${days}d ago"
            else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
        }
    } catch (e: Exception) {
        return dateString
    }
}

fun isToday(dateString: String?): Boolean {
    if (dateString == null) return false
    try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString) ?: return false
        val todayCal = Calendar.getInstance()
        val dateCal = Calendar.getInstance().apply { time = date }
        return todayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
               todayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
    } catch (e: Exception) { return false }
}

fun isYesterday(dateString: String?): Boolean {
    if (dateString == null) return false
    try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString) ?: return false
        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val dateCal = Calendar.getInstance().apply { time = date }
        return yesterdayCal.get(Calendar.YEAR) == dateCal.get(Calendar.YEAR) &&
               yesterdayCal.get(Calendar.DAY_OF_YEAR) == dateCal.get(Calendar.DAY_OF_YEAR)
    } catch (e: Exception) { return false }
}

fun isThisWeek(dateString: String?): Boolean {
    if (dateString == null) return false
    try {
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val date = format.parse(dateString) ?: return false
        val weekAgoCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
        return date.time >= weekAgoCal.timeInMillis
    } catch (e: Exception) { return false }
}
