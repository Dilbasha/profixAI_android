package com.simats.profixai.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*

@Composable
fun AdminDashboardStatsScreen(navController: NavController) {
    val tealColor = Color(0xFF009688)
    val darkBg = Color(0xFF102A2E)
    val cardBg = Color(0xFF1A383D)
    
    var stats by remember { mutableStateOf<AdminStats?>(null) }
    var recentActivity by remember { mutableStateOf<List<RecentActivity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getAdminStats()
            if (response.isSuccessful && response.body()?.success == true) {
                stats = response.body()?.stats
                recentActivity = response.body()?.recent_activity ?: emptyList()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    Column(modifier = Modifier.fillMaxSize().background(tealColor)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Dashboard Overview", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Stats Row 1: Users and Providers
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Users",
                            value = "${stats?.total_users ?: 0}",
                            icon = Icons.Default.People,
                            color = cardBg,
                            iconColor = Color(0xFF4FC3F7)
                        )
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Providers",
                            value = "${stats?.approved_providers ?: 0}",
                            icon = Icons.Default.Engineering,
                            color = cardBg,
                            iconColor = Color(0xFF81C784)
                        )
                    }
                }
                
                // Stats Row 2: Bookings and Revenue
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Total Bookings",
                            value = "${stats?.total_bookings ?: 0}",
                            icon = Icons.Default.CalendarMonth,
                            color = cardBg,
                            iconColor = Color(0xFFFFB74D)
                        )
                        StatsCard(
                            modifier = Modifier.weight(1f),
                            title = "Revenue",
                            value = "₹${((stats?.total_revenue ?: 0.0) / 1000).toInt()}K",
                            icon = Icons.Default.CurrencyRupee,
                            color = cardBg,
                            iconColor = Color(0xFF4CAF50)
                        )
                    }
                }
                
                // Pending Approvals Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFFFC107).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.HowToReg, null, Modifier.size(24.dp), Color(0xFFFFC107))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Pending Approvals", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                Text("${stats?.pending_providers ?: 0}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                
                // Booking Status Overview
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Booking Status", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            val total = (stats?.total_bookings ?: 1).coerceAtLeast(1)
                            val completedProgress = (stats?.completed_bookings ?: 0).toFloat() / total
                            val activeProgress = (stats?.active_bookings ?: 0).toFloat() / total
                            val pendingProgress = (stats?.pending_bookings ?: 0).toFloat() / total
                            
                            TaskProgressRow("Completed (${stats?.completed_bookings ?: 0})", completedProgress, Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.height(8.dp))
                            TaskProgressRow("Active (${stats?.active_bookings ?: 0})", activeProgress, Color(0xFF2196F3))
                            Spacer(modifier = Modifier.height(8.dp))
                            TaskProgressRow("Pending (${stats?.pending_bookings ?: 0})", pendingProgress, Color(0xFFFFC107))
                        }
                    }
                }
                
                // Hours Worked Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFF7C4DFF).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, null, Modifier.size(24.dp), Color(0xFF7C4DFF))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Total Hours Worked", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                Text("${(stats?.total_hours_worked ?: 0.0).toInt()} hrs", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                
                // Recent Activity
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Recent Activity", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (recentActivity.isEmpty()) {
                                Text("No recent activity", color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
                            } else {
                                recentActivity.take(5).forEachIndexed { index, activity ->
                                    val (icon, iconColor) = when (activity.status) {
                                        "completed" -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
                                        "in_progress" -> Icons.Default.PlayCircle to Color(0xFF2196F3)
                                        "accepted" -> Icons.Default.ThumbUp to Color(0xFF4FC3F7)
                                        "pending" -> Icons.Default.Pending to Color(0xFFFFC107)
                                        else -> Icons.Default.Info to Color.Gray
                                    }
                                    ActivityItem(
                                        icon = icon, 
                                        iconColor = iconColor, 
                                        text = "${activity.user_name ?: "User"} booked ${activity.service_name ?: "service"} with ${activity.provider_name ?: "provider"}", 
                                        time = activity.booking_date ?: ""
                                    )
                                    if (index < recentActivity.size - 1) {
                                        Spacer(modifier = Modifier.height(12.dp))
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

@Composable
fun StatsCard(modifier: Modifier, title: String, value: String, icon: ImageVector, color: Color, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, Modifier.size(20.dp), iconColor)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun TaskProgressRow(label: String, progress: Float, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text("${(progress * 100).toInt()}%", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF102A2E)
        )
    }
}

@Composable
fun ActivityItem(icon: ImageVector, iconColor: Color, text: String, time: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(16.dp), iconColor)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
            Text(time, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
    }
}
