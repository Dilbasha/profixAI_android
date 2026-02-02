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
import androidx.compose.ui.draw.clip
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
fun UserBookingsScreen(navController: NavController, userId: Int) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getUserBookings(UserIdRequest(user_id = userId))
            if (response.isSuccessful && response.body()?.success == true) {
                bookings = response.body()?.bookings ?: emptyList()
            }
        } catch (e: Exception) {
            // Handle error
        } finally {
            isLoading = false
        }
    }
    
    val filteredBookings = when (selectedTab) {
        0 -> bookings
        1 -> bookings.filter { it.status in listOf("pending", "accepted", "in_progress") }
        2 -> bookings.filter { it.status == "completed" }
        else -> bookings
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Bookings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Gray50)
        ) {
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF0D9997)
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("All", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Active", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                    Text("Completed", modifier = Modifier.padding(16.dp))
                }
            }
            
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF0D9997))
                    }
                }
                filteredBookings.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = Gray400
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No bookings found", fontSize = 18.sp, color = Gray600)
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredBookings) { booking ->
                            BookingCard(
                                booking = booking,
                                navController = navController,
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
}

@Composable
fun BookingCard(booking: Booking, navController: NavController? = null, onRate: (() -> Unit)? = null) {
    val statusColor = when (booking.status) {
        "pending" -> Orange500
        "accepted" -> Color(0xFF0D9997)
        "in_progress" -> Amber500
        "completed" -> Green500
        "cancelled" -> Red500
        else -> Gray500
    }
    
    val canTrackLocation = booking.status in listOf("accepted", "in_progress")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileAvatar(
                    imageUrl = booking.provider_image,
                    name = booking.provider_name ?: "P",
                    size = 50.dp,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.provider_name ?: "Provider",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900
                    )
                    Text(
                        text = booking.service_name ?: "",
                        fontSize = 14.sp,
                        color = Gray600
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = booking.status.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = statusColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Gray200)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.booking_date, fontSize = 14.sp, color = Gray700)
                Spacer(modifier = Modifier.width(16.dp))
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.booking_time, fontSize = 14.sp, color = Gray700)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp), tint = Gray500)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = booking.address, fontSize = 14.sp, color = Gray700, maxLines = 1)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${booking.estimated_hours} hour(s)", fontSize = 14.sp, color = Gray600)
                Text(
                    text = "₹${booking.total_amount.toInt()}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D9997)
                )
            }
            
            // Track Location button for accepted/in_progress bookings
            if (canTrackLocation && navController != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { navController.navigate("location_tracking/${booking.id}") },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9997))
                ) {
                    Icon(Icons.Default.LocationOn, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Track Provider Location", fontWeight = FontWeight.SemiBold)
                }
            }
            
            // Rate button for completed bookings
            if (onRate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onRate,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                ) {
                    Icon(Icons.Default.Star, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Rate This Service", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


