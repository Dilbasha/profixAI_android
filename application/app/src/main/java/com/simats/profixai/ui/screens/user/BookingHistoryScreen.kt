package com.simats.profixai.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.components.ProfileAvatar
import com.simats.profixai.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingHistoryScreen(navController: NavController, userId: Int) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    LaunchedEffect(userId) {
        try {
            val response = RetrofitClient.apiService.getUserBookings(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                bookings = response.body()?.bookings ?: emptyList()
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    val filteredBookings = when (selectedTab) {
        0 -> bookings
        1 -> bookings.filter { it.status == "completed" }
        2 -> bookings.filter { it.status == "cancelled" }
        else -> bookings
    }
    
    val totalSpent = bookings.filter { it.status == "completed" }.sumOf { it.total_amount }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Booking History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D9997),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).background(Gray50)
        ) {
            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(Modifier.fillMaxWidth().padding(20.dp), Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${bookings.size}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9997))
                        Text("Total Bookings", fontSize = 12.sp, color = Gray600)
                    }
                    Divider(Modifier.width(1.dp).height(40.dp), color = Gray200)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${bookings.count { it.status == "completed" }}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Green500)
                        Text("Completed", fontSize = 12.sp, color = Gray600)
                    }
                    Divider(Modifier.width(1.dp).height(40.dp), color = Gray200)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("₹${totalSpent.toInt()}", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Amber500)
                        Text("Total Spent", fontSize = 12.sp, color = Gray600)
                    }
                }
            }
            
            // Filter Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0D9997)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("All", Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Completed", Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Cancelled", Modifier.padding(16.dp))
                }
            }
            
            if (isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0D9997))
                }
            } else if (filteredBookings.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, Modifier.size(64.dp), Gray400)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No bookings found", color = Gray500)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings) { booking ->
                        HistoryBookingCard(
                            booking = booking,
                            onRate = if (booking.status == "completed") {
                                { navController.navigate("rating/$userId/${booking.id}") }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryBookingCard(booking: Booking, onRate: (() -> Unit)? = null) {
    val statusColor = when (booking.status) {
        "completed" -> Green500
        "cancelled" -> Red500
        "pending" -> Orange500
        else -> Gray500
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ProfileAvatar(
                    imageUrl = booking.provider_image,
                    name = booking.provider_name ?: "P",
                    size = 48.dp,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(booking.provider_name ?: "Provider", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Gray900)
                    Text(booking.service_name ?: "", fontSize = 13.sp, color = Gray600)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("₹${booking.total_amount.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9997))
                    Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.1f)) {
                        Text(
                            booking.status.replaceFirstChar { it.uppercase() },
                            Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp, color = statusColor
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, Modifier.size(14.dp), Gray500)
                Text(" ${booking.booking_date}", fontSize = 12.sp, color = Gray600)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, null, Modifier.size(14.dp), Gray500)
                Text(" ${booking.booking_time}", fontSize = 12.sp, color = Gray600)
            }
            
            if (onRate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onRate,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Amber500)
                ) {
                    Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rate This Service")
                }
            }
        }
    }
}
