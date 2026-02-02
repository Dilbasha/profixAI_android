package com.simats.profixai.ui.screens.provider

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.simats.profixai.network.*
import com.simats.profixai.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EarningsHistoryScreen(navController: NavController, providerId: Int) {
    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    var stats by remember { mutableStateOf<ProviderStats?>(null) }
    
    LaunchedEffect(providerId) {
        try {
            val bookingsResponse = RetrofitClient.apiService.getProviderBookings(ProviderIdRequest(provider_id = providerId))
            if (bookingsResponse.isSuccessful && bookingsResponse.body()?.success == true) {
                bookings = bookingsResponse.body()?.bookings?.filter { it.status == "completed" } ?: emptyList()
            }
            
            val statsResponse = RetrofitClient.apiService.getProviderStats(ProviderIdRequest(provider_id = providerId))
            if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                stats = statsResponse.body()?.stats
            }
        } catch (e: Exception) { }
        finally { isLoading = false }
    }
    
    // Filter by tab
    val filteredBookings = remember(bookings, selectedTab) {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        
        when (selectedTab) {
            0 -> { // This Week
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                bookings.filter { 
                    try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.booking_date)
                        date != null && date.time >= weekAgo
                    } catch (e: Exception) { true }
                }
            }
            1 -> { // This Month
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.timeInMillis
                bookings.filter {
                    try {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.booking_date)
                        date != null && date.time >= monthAgo
                    } catch (e: Exception) { true }
                }
            }
            else -> bookings // All Time
        }
    }
    
    val totalEarnings = filteredBookings.sumOf { it.total_amount }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Earnings History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0D1B2A),
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
                .background(Color(0xFF0D1B2A))
        ) {
            // Tab Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("This Week", "This Month", "All Time").forEachIndexed { index, title ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(title, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Blue600,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1B2838),
                            labelColor = Color.White
                        )
                    )
                }
            }
            
            // Total Earnings Card
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Blue600)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total Earnings", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                when(selectedTab) {
                                    0 -> "This Week"
                                    1 -> "This Month"
                                    else -> "All Time"
                                },
                                Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 12.sp, color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "₹${String.format("%.2f", totalEarnings)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, Modifier.size(16.dp), Green500)
                            Text(" ${filteredBookings.size} completed jobs", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Recent Transactions",
                Modifier.padding(horizontal = 16.dp),
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    CircularProgressIndicator(color = Blue600)
                }
            } else if (filteredBookings.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ReceiptLong, null, Modifier.size(64.dp), Gray500)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No transactions yet", color = Gray500)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBookings) { booking ->
                        EarningItem(booking)
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun EarningItem(booking: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B2838))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Blue600.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Build, null, Modifier.size(22.dp), Blue600)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    booking.service_name ?: "Service",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(booking.booking_date, fontSize = 12.sp, color = Gray500)
                    Text(" • ", fontSize = 12.sp, color = Gray500)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Green500.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Paid",
                            Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp, color = Green500
                        )
                    }
                }
            }
            
            Text(
                "+₹${booking.total_amount.toInt()}.00",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green500
            )
        }
    }
}
